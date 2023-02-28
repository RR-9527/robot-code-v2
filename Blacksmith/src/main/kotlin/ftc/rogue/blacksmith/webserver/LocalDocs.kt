@file:Suppress("MayBeConstant")

package ftc.rogue.blacksmith.webserver

import android.content.Context
import com.qualcomm.robotcore.util.WebHandlerManager
import fi.iki.elonen.NanoHTTPD
import org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar

const val REDIRECT_URL = "https://blacksmithftc.vercel.app/"

const val HTML = """
    <!DOCTYPE html>
    <html>
        <head>
            <title>Old Page</title>
         <meta charset="UTF-8" />
         <meta http-equiv="refresh" content="3; URL="$REDIRECT_URL" />
       </head>
       <body>
         <p>This page has been moved. If you are not redirected within 3 seconds, click <a href="$REDIRECT_URL">here</a> to go to the HubSpot homepage.</p>
       </body>
    </html>
"""

@WebHandlerRegistrar
fun registerDocs(context: Context, manager: WebHandlerManager) {
    if (manager.webServer == null)
        return

    val commands = listOf("/blacksmith", "/blacksmith/", "/blacksmithftc", "/blacksmithftc/")

    with(manager) {
        commands.forEach(::registerUrl)
    }
}

val statusOk = NanoHTTPD.Response.Status.OK
val mimeHtml = NanoHTTPD.MIME_HTML

// TODO: See if the FixedLengthResponse can be reused
fun WebHandlerManager.registerUrl(command: String) =
    register(command) { _ ->
        NanoHTTPD.newFixedLengthResponse(statusOk, mimeHtml, HTML)
    }
