package be.robinj.rapid7.insight.agent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permission {
	private final Context context;
	private final String permission;

	private final static String[] BASIC_PERMISSIONS = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.INTERNET,
			Manifest.permission.ACCESS_NETWORK_STATE,
			Manifest.permission.FOREGROUND_SERVICE,
			Manifest.permission.READ_LOGS
	};

	public Permission(final Context context, final String permission) {
		this.context = context;
		this.permission = permission;
	}

	public boolean check() {
		return ContextCompat.checkSelfPermission(this.context, this.permission) == PackageManager.PERMISSION_GRANTED;
	}

	public Permission request(final Activity parent) {
		if (! this.check()) {
			this.requestPermission(parent);
		}

		return this;
	}

	public static void requestBasicPermissions(final Activity parent) {
		requestMultiple(parent, BASIC_PERMISSIONS);
	}

	public static boolean hasBasicPermissions(final Context context) {
		for (final String permission : BASIC_PERMISSIONS) {
			if (! new Permission(context, permission).check()) {
				return false;
			}
		}

		return true;
	}

	public static void requestMultiple(final Activity parent, final String[] permissions) {
		final List<String> permissionsToRequest = new ArrayList<>();
		for (final String permission : permissions) {
			if (! new Permission(parent, permission).check()) {
				permissionsToRequest.add(permission);
			}
		}

		if (permissionsToRequest.isEmpty()) {
			return;
		}

		ActivityCompat.requestPermissions(parent, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), 1);
	}

	private void requestPermission(final Activity parent) {
		ActivityCompat.requestPermissions(parent, new String[] { this.permission }, 1);
	}
}
