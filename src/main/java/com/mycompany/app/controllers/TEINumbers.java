package com.mycompany.app.controllers;

import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.mycompany.app.controllers.XSLTTransformer;

/** Adds line numbers to a TEI document.
 *
 * Line numbers are added as n attributes to tei:l elements.
 * An xml:id is also added to the element like line-${line_number}
 */
public class TEINumbers extends XSLTTransformer
{
    @Override
    public void init ()
    {
        xsltResourceName = "tei-numbers.xslt";
    }
}
