package io.github.bl3xand.apkcloner.install.methods

import io.github.bl3xand.apkcloner.compat.installerOf
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import io.github.bl3xand.apkcloner.data.ApkSource
import io.github.bl3xand.apkcloner.install.Installer
import io.github.bl3xand.apkcloner.install.InstallOutcome
import io.github.bl3xand.apkcloner.install.PlayProtect
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The system's session installer. Silent only under the platform's own conditions. */
object StockInstaller : Installer {

    /**
     * A clone is re-signed, and with Play Protect scanning on such an app tends to be blocked -
     * with a full-screen dialog, which a background update must never cause.
     */
    override fun canInstallSilently(context: Context, app: ApkSource): Boolean =
        PlayProtect.isEnabled(context) != true && isUpdateWithoutPrompt(context, app.packageName, app.appInfo.targetSdkVersion)

    /**
     * Whether the system installs an update of [packageName] without asking: it does so only
     * for the installer of record, and only for apps that target a recent enough SDK. Apps
     * that keep their publisher's signature are not held back by Play Protect.
     */
    fun isUpdateWithoutPrompt(context: Context, packageName: String, targetSdk: Int): Boolean {
        // Before Android 12 the system asks about every install, whoever starts it.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        val pm = context.packageManager
        val owner = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runCatching { pm.getInstallSourceInfo(packageName).updateOwnerPackageName }.getOrNull()
        } else null
        val installedByUs = if (owner != null) owner == context.packageName else pm.installerOf(packageName) == context.packageName
        return installedByUs && targetSdk >= minimumTargetForNoUserAction(Build.VERSION.SDK_INT)
    }

    internal fun minimumTargetForNoUserAction(platformSdk: Int): Int = when {
        platformSdk >= 37 -> 35 // Android 17 / API 37, as documented at the time of this change.
        platformSdk >= 36 -> 34
        platformSdk >= 35 -> 33
        platformSdk >= 34 -> 31
        platformSdk >= 33 -> 30
        else -> 29
    }

    override suspend fun install(
        context: Context, apks: List<File>, background: Boolean, label: String, installerPackage: String?,
        requestUpdateOwnership: Boolean,
    ): InstallOutcome =
        try {
            withContext(Dispatchers.IO) {
                InstallOutcome.Pending(ApkInstaller.install(context, apks, background, label, requestUpdateOwnership))
            }
        } catch (e: Exception) {
            InstallOutcome.Failed(e.message ?: e.javaClass.simpleName)
        }

}
