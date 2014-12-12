package tk.hocl.controllers;

import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import tk.hocl.controllers.XSLTTransformer;

/** Transforms a TEI document into an HTML page.
 *
 * If there is analysis included in the document text, then it may
 * be included in the HTML page. See the stylesheet for a description
 * of how the kinds of analysis text that will be interpreted.
 */
public class TEIHtml extends XSLTTransformer
{

    {
        xsltResourceName = "tei-html.xslt";
        contentType = "text/html";
        params.put("line_number_interval", "5");
    }
}

