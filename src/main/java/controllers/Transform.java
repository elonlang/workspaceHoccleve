package controllers;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Writer;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.ErrorListener;

import util.Util;

public class Transform
{
    static SAXTransformerFactory tf;
    static ErrorListener errors = new MyErrorListener();

    static {
        TransformerFactory tf_instance = TransformerFactory.newInstance();
        if (tf_instance.getFeature(SAXTransformerFactory.FEATURE))
        {
            tf = (SAXTransformerFactory)tf_instance;
        }

        tf.setErrorListener(errors);
    };

    /* A wrapper around a checked exception, TransformerException,
     * so it can pass through the to top level methods
     */
    public static class MyTransformerException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public TransformerException exc;
        public MyTransformerException (TransformerException tf)
        {
            super();
            exc = tf;
        }
    }

    public static class MyErrorListener implements ErrorListener
    {
        public void fatalError(TransformerException exception) throws TransformerException
        {
            throw exception;
        }

        public void warning(TransformerException exception) throws TransformerException
        {
            System.err.println("transformation warning: ");
            exception.printStackTrace();
        }

        public void error(TransformerException exception)
        {
            System.err.println("transformation error: ");
            exception.printStackTrace();
        }
    }

    public static Source getInput(String fileName)
    {
        Source res = null;
        try
        {
            res = getInput(new FileInputStream(fileName));
        }
        catch (FileNotFoundException e)
        {/* fall through */}
        return res;
    }

    public static Source getInputFromURL(String url)
    {
        return getInput(Util.openURL(url));
    }

    public static Source getInput(InputStream is)
    {
        return new StreamSource(is);
    }

    public static Result getOutput(String fileName)
    {
        Result res = null;
        try
        {
            res = getOutput(new FileOutputStream(fileName));
        }
        catch (FileNotFoundException e)
        {/* fall through */}
        return res;
    }

    public static Result getOutput(OutputStream os)
    {
        return new StreamResult(os);
    }

    public static Result getOutput(Writer w)
    {
        return new StreamResult(w);
    }

    public static Transformer getTransformerFromURL(String url)
    {
        Source xslt_in = getInput(Util.openURL(url));
        Transformer res = null;
        try
        {
            res = tf.newTransformer(xslt_in);
        }
        catch (TransformerConfigurationException e)
        {
            throw new MyTransformerException(e);
        }
        return res;
    }

    public static Templates getTemplatesFromURL(String url)
    {
        Source xslt_in = getInput(Util.openURL(url));
        Templates res = null;
        try
        {
            res = tf.newTemplates(xslt_in);
        }
        catch (TransformerConfigurationException e)
        {
            throw new MyTransformerException(e);
        }
        return res;
    }


    public static Transformer getTransformer(InputStream in)
    {
        Source xslt_in = getInput(in);
        return getTransformer(xslt_in);
    }

    public static Transformer getTransformer(String fileName)
    {
        Source xslt_in = getInput(fileName);
        return getTransformer(xslt_in);
    }

    public static Transformer getTransformer(Source in)
    {
        Transformer res = null;
        try
        {
            res = tf.newTransformer(in);
        }
        catch (TransformerConfigurationException e)
        {
            throw new MyTransformerException(e);
        }
        return res;
    }

    public static void doTransformation(Transformer t, Source in, Result out, Map<String,Object> params)
    {
        for (String p : params.keySet())
        {
            t.setParameter(p, params.get(p));
        }
        doTransformation(t, in, out);
    }

    public static void doTransformation(Transformer t, Source in, Result out)
    {
        try
        {
            t.transform(in, out);
        }
        catch (TransformerException e)
        {
            throw new MyTransformerException(e);
        }
    }

    public static void doTransformation(Transformer t, InputStream in, Result out)
    {
        doTransformation(t, getInput(in), out);
    }

    public static void doTransformation(Transformer t, InputStream in, OutputStream out)
    {
        doTransformation(t, getInput(in), getOutput(out));
    }

    public static void doTransformation(String t, InputStream in, OutputStream out, Map<String, Object> params)
    {
        Transformer tf = getTransformerFromURL(t);
        doTransformation(tf, getInput(in), getOutput(out), params);
    }

    public static void doTransformation(String t, InputStream in, OutputStream out)
    {
        Transformer tf = getTransformerFromURL(t);
        doTransformation(tf, getInput(in), getOutput(out));
    }

    public static void doTransformation(String xslt, String in, Writer out)
    {
        Source xml_in = getInput(Util.openURL(in));
        Result xml_out = getOutput(out);
        Transformer tf = getTransformerFromURL(xslt);
        doTransformation(tf, xml_in, xml_out);
    }

    public static void doTransformation(String xslt, InputStream in, Writer out, Map<String,Object> params)
    {
        Source xml_in = getInput(in);
        Result xml_out = getOutput(out);
        Transformer tf = getTransformerFromURL(xslt);
        doTransformation(tf, xml_in, xml_out, params);
    }

    public static void doTransformation(String xslt, InputStream in, Writer out)
    {
        Source xml_in = getInput(in);
        Result xml_out = getOutput(out);
        Transformer tf = getTransformerFromURL(xslt);
        doTransformation(tf, xml_in, xml_out);
    }

    public static void doTransformation(String[] xslts, InputStream in, OutputStream out)
    {
        /* Perform a series of transformations */
        Templates[] ts = new Templates[xslts.length];
        for (int i = 0; i < xslts.length; i++)
        {
            ts[i] = getTemplatesFromURL(xslts[i]);
        }
        doTransformation(ts, getInput(in), getOutput(out));
    }

    public static void doTransformation(Templates[] ts, Source in, Result out)
    {
        /* This loop performs 1 or more transformations given in the array `tfs` */
        if (ts.length == 1)
        {
            try
            {
                doTransformation(ts[0].newTransformer(), in, out);
            }
            catch (TransformerConfigurationException e)
            {
                throw new MyTransformerException(e);
            }
        }
        else
        {
            TransformerHandler[] handlers = new TransformerHandler[ts.length];

            try
            {

                for (int i = 0; i < ts.length; i++)
                {
                    handlers[i] = tf.newTransformerHandler(ts[i]);
                }

                for (int i = 0; i < ts.length - 1; i++)
                {
                    handlers[i].setResult(new SAXResult(handlers[i+1]));
                }
                handlers[ts.length - 1].setResult(out);

                Transformer t = tf.newTransformer();
                t.transform(in, new SAXResult(handlers[0]));
            }
            catch (TransformerConfigurationException e)
            {
                System.err.println("Error creating templates");
                e.printStackTrace();
            }
            catch (TransformerException e)
            {
                System.err.println("Error transforming");
                e.printStackTrace();
            }

        }
    }

    public static void doTransformation(InputStream xslt, InputStream in, OutputStream out)
    {
        Source xml_in = getInput(in);
        Result xml_out = getOutput(out);
        Transformer tf = getTransformer(xslt);
        doTransformation(tf, xml_in, xml_out);
    }

    /** Takes the URI of the xslt and input xml and writes to the OutputStream.
    */
    public static void doTransformation(String xslt, String in, OutputStream out)
    {
        Source xml_in = getInput(Util.openURL(in));
        Result xml_out = getOutput(out);
        Transformer tf = getTransformerFromURL(xslt);
        doTransformation(tf, xml_in, xml_out);
    }

    public static void main (String [] args)
    {
        String xslt = args[0];
        String fname = args[1];
        Source xml_in = getInput(fname);
        Result xml_out = getOutput(fname + ".out");
        Transformer tf = getTransformer(xslt);
        doTransformation(tf, xml_in, xml_out);
    }
}
