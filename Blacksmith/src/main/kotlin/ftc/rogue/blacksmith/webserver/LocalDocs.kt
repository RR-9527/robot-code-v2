package ftc.rogue.blacksmith.webserver

import android.content.Context
import com.qualcomm.robotcore.util.WebHandlerManager
import fi.iki.elonen.NanoHTTPD
import org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar
import org.firstinspires.ftc.robotcore.internal.system.AppUtil

@WebHandlerRegistrar
fun registerDocs(
    context: Context,
    manager: WebHandlerManager
) {
    if (manager.webServer == null)
        return

    val assets = AppUtil.getInstance().activity?.assets
        ?: return

    val statusOk = NanoHTTPD.Response.Status.OK
    val mimeHtml = NanoHTTPD.MIME_HTML

    manager.register("/blacksmith/") {
        NanoHTTPD.newFixedLengthResponse(statusOk, mimeHtml, "")
    }
}
