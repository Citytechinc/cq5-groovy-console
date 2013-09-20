package com.citytechinc.cq.groovyconsole.servlets

import com.day.cq.commons.jcr.JcrConstants
import groovy.json.JsonBuilder
import org.apache.felix.scr.annotations.Activate
import org.apache.felix.scr.annotations.Deactivate
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.sling.SlingServlet
import org.apache.sling.api.SlingHttpServletRequest
import org.apache.sling.api.SlingHttpServletResponse
import org.apache.sling.jcr.api.SlingRepository

@SlingServlet(paths = "/bin/groovyconsole/save")
class ScriptSavingServlet extends AbstractScriptServlet {

    static final String SCRIPT_FOLDER_REL_PATH = "scripts"

    static final String CONSOLE_ROOT = "/etc/groovyconsole"

    static final String FILE_NAME_PARAM = "fileName"

    static final String SCRIPT_CONTENT_PARAM = "scriptContent"

    static final String EXTENSION_GROOVY = ".groovy"

    @Reference
    SlingRepository repository

    def session

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        def name = request.getParameter(FILE_NAME_PARAM)
        def script = request.getParameter(SCRIPT_CONTENT_PARAM)

        def folderNode = session.getNode(CONSOLE_ROOT).getOrAddNode(SCRIPT_FOLDER_REL_PATH, JcrConstants.NT_FOLDER)

        def fileName = name.endsWith(EXTENSION_GROOVY) ? name : "$name$EXTENSION_GROOVY"

        folderNode.removeNode(fileName)

        getScriptBinary(script).withBinary { binary ->
            saveFile(session, folderNode, fileName, "application/octet-stream", binary)
        }

        response.contentType = "application/json"

        new JsonBuilder([scriptName: fileName]).writeTo(response.writer)
    }

    def getScriptBinary(script) {
        def binary = null

        new ByteArrayInputStream(script.getBytes("UTF-8")).withStream { stream ->
            binary = session.valueFactory.createBinary(stream)
        }

        binary
    }

    @Activate
    void activate() {
        session = repository.loginAdministrative(null)
    }

    @Deactivate
    void deactivate() {
        session?.logout()
    }
}