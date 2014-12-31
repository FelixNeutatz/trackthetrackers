package io.ssc.trackthetrackers.extraction.resources;

import com.google.common.collect.Sets;

import org.mozilla.javascript.ast.*;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.ast.Name;


import org.apache.commons.validator.routines.DomainValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;


public class RhinoParserExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(RhinoParserExtractor.class);

    private final URLNormalizer urlNormalizer = new URLNormalizer();


    private static Set<String> EXTRA_ANNOTATIONS = new HashSet<String>(Arrays.asList(
            "suppressReceiverCheck",
            "suppressGlobalPropertiesCheck"
    ));


    public synchronized Iterable<Resource> extractResources(String sourceUrl, String html) {

        ArrayList<String> scriptHtml = new ArrayList<String>();

        Set<Resource> resources = Sets.newHashSet();
        String prefixForInternalLinks = urlNormalizer.createPrefixForInternalLinks(sourceUrl);

        Document doc = Jsoup.parse(html);
        Elements iframes = doc.select("iframe[src]");
        Elements links = doc.select("link[href]");
        Elements imgs = doc.select("img[src]");
        Elements scripts = doc.select("script");

        Elements all = iframes.clone();
        all.addAll(scripts);
        all.addAll(links);
        all.addAll(imgs);

        String uri = null;

        for (Element tag: all) {
            uri = tag.attr("src");

            if (!uri.contains(".")) {
                uri = tag.attr("href");
            }

            if (uri.contains(".")) {
                uri = urlNormalizer.expandIfInternalLink(prefixForInternalLinks, uri);
                // normalize link
                try {
                    uri = urlNormalizer.normalize(uri);
                    uri = urlNormalizer.extractDomain(uri);
                } catch (MalformedURLException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Malformed URL: \"" + uri + "\"");
                    }
                }
                if (isValidDomain(uri)) {
                    resources.add(new Resource(uri, type(tag.tag().toString())));
                }
            }


            if (tag.tag().toString().equals("script")) { //filter functions
                if(tag.data().length() > 1) {
                    scriptHtml.add(tag.data());
                    //System.out.println("script: \n" + tag.data());
                }
            }
        }

        CompilerEnvirons environment = new CompilerEnvirons();


        ErrorReporter testErrorReporter = new ErrorReporter() {
            @Override
            public void warning(String s, String s1, int i, String s2, int i1) {
                //System.out.println(s + s1 + i + s2 + i1);
            }

            @Override
            public void error(String s, String s1, int i, String s2, int i1) {
                //System.out.println(s + s1 + i + s2 + i1);
            }

            @Override
            public EvaluatorException runtimeError(String s, String s1, int i, String s2, int i1) {
                return null;
            }
        };
        environment.setErrorReporter(testErrorReporter);

        environment.setRecordingComments(true);
        environment.setRecordingLocalJsDocComments(true);

        environment.setRecoverFromErrors(true);

        for(String script:scriptHtml) {
            if(script != null) {
                try {
                    Parser p = new Parser(environment, testErrorReporter);
                    AstRoot root = null;


                    root = p.parse(script, null, 0);

                    if (root != null) {

                        StringBuilder sb = new StringBuilder();
                        DebugPrintVisitor visitor = new DebugPrintVisitor(sb);
                        root.visitAll(visitor);
                        System.out.println(sb.toString());
                    }
                } catch(Exception e) {
                    System.out.println(e);
                }
            }

        }




        return null;

       // return resources;
    }



    private boolean isValidDomain(String url) {
        if (!url.contains(".") || url.contains("///")) {
            return false;
        }

        if (url.contains(";") || url.contains("=") || url.contains("?")) {
            return false;
        }

        int startTopLevelDomain = url.lastIndexOf('.');
        String topLevelDomain = url.substring(startTopLevelDomain + 1);
        return DomainValidator.getInstance().isValidTld(topLevelDomain);
    }


    private Resource.Type type(String tag) {
        if ("script".equals(tag)) {
            return Resource.Type.SCRIPT;
        }
        if ("link".equals(tag)) {
            return Resource.Type.LINK;
        }
        if ("img".equals(tag)) {
            return Resource.Type.IMAGE;
        }
        if ("iframe".equals(tag)) {
            return Resource.Type.IFRAME;
        }

        return Resource.Type.OTHER;
    }

    protected static class DebugPrintVisitor implements NodeVisitor {
        private StringBuilder buffer;
        private static final int DEBUG_INDENT = 2;
        public DebugPrintVisitor(StringBuilder buf) {
            buffer = buf;
        }
        public String toString() {
            return buffer.toString();
        }
        private String makeIndent(int depth) {
            StringBuilder sb = new StringBuilder(DEBUG_INDENT * depth);
            for (int i = 0; i < (DEBUG_INDENT * depth); i++) {
                sb.append(" ");
            }
            return sb.toString();
        }
        public boolean visit(AstNode node) {
            int tt = node.getType();
            String name = Token.typeToName(tt);
            buffer.append(node.getAbsolutePosition()).append("\t");
            buffer.append(makeIndent(node.depth()));
            buffer.append(name).append(" ");
            buffer.append(node.getPosition()).append(" ");
            buffer.append(node.getLength());
            if (tt == Token.NAME) {
                buffer.append(" ").append(((Name)node).getIdentifier());
            }

            /*
            if(node instanceof FunctionCall){
                AstNode target = ((FunctionCall) node).getTarget();

                if(target instanceof Name) {
                    System.out.println("function:" + ((Name) target).getIdentifier());
                }
            }
            */

            buffer.append("\n");
            return true;  // process kids
        }
    }
}
