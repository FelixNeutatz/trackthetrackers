package io.ssc.trackthetrackers.extraction.resources;

import com.google.common.collect.Sets;
import org.apache.commons.validator.routines.DomainValidator;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.Scanner;

public class GhostDriverExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(GhostDriverExtractor.class);

    private final URLNormalizer urlNormalizer = new URLNormalizer();


    /*

    public Iterable<Resource> extractResources1(String sourceUrl, String html) {

        Capabilities capabilities = new DesiredCapabilities().phantomjs();
        // Set PhantomJS Path
        ((DesiredCapabilities) capabilities).setCapability("phantomjs.binary.path", "/home/felix/Software/phantomjs/bin/phantomjs");
        ((DesiredCapabilities) capabilities).setCapability("phantomjs.page.settings.loadImages", false);

        WebDriver d = new PhantomJSDriver(capabilities);


        if (!(d instanceof PhantomJSDriver)) {
            // Skip this test if not using PhantomJS.
            // The command under test is only available when using PhantomJS
            return null;
        }

        PhantomJSDriver phantom = (PhantomJSDriver) d;

        LogEntries logs = phantom.manage().logs().get("browser");

        Object result = phantom.executePhantomJS("var resourceWait  = 300,\n" +
                "      maxRenderWait = 10000;\n" +
                "\n" +
                "  var page          = this,\n" +
                "      count         = 0,\n" +
                "      forcedRenderTimeout,\n" +
                "      renderTimeout;\n" +
                "\n" +
                "  page.viewportSize = { width: 1280,  height : 1024 };\n" +
                "\n" +
                "  function doRender() {\n" +
                "\n" +
                "  }\n" +
                "\n" +
                "  page.onResourceRequested = function (req) {\n" +
                "      count += 1;\n" +
                "      console.log('> ' + req.id + ' - ' + req.url);\n" +
                "      clearTimeout(renderTimeout);\n" +
                "  };\n" +
                "\n" +
                "  page.onResourceReceived = function (res) {\n" +
                "      if (!res.stage || res.stage === 'end') {\n" +
                "          count -= 1;\n" +
                "          console.log(res.id + ' ' + res.status + ' - ' + res.url);\n" +
                "          if (count === 0) {\n" +
                "              renderTimeout = setTimeout(doRender, resourceWait);\n" +
                "          }\n" +
                "      }\n" +
                "  };");

        phantom.get("file:///home/felix/trackthetrackers/extraction/src/test/resources/zalando.de.html");



        for(String s : phantom.manage().logs().getAvailableLogTypes()){
            System.out.println("type: " + s);
        }

        for(LogEntry log :logs.getAll()) {
            System.out.println(log.getMessage());
        }





        return null;
    }

*/
/*
    public Iterable<Resource> extractResources2(String sourceUrl, String html) {


        Capabilities capabilities = new DesiredCapabilities().phantomjs();
        // Set PhantomJS Path
        ((DesiredCapabilities) capabilities).setCapability("phantomjs.binary.path", "/home/felix/Software/phantomjs/bin/phantomjs");
        ((DesiredCapabilities) capabilities).setCapability("phantomjs.page.settings.loadImages", false);

        WebDriver d = new PhantomJSDriver(capabilities);


        if (!(d instanceof PhantomJSDriver)) {
            // Skip this test if not using PhantomJS.
            // The command under test is only available when using PhantomJS
            return null;
        }

        PhantomJSDriver phantom = (PhantomJSDriver) d;

        Object result = phantom.executePhantomJS("var resourceWait  = 300,\n" +
                "      maxRenderWait = 10000;\n" +
                "\n" +
                "  var page          = this,\n" +
                "      count         = 0,\n" +
                "      forcedRenderTimeout,\n" +
                "      renderTimeout;\n" +
                "\n" +
                "  page.viewportSize = { width: 1280,  height : 1024 };\n" +
                "\n" +
                "  function doRender() {\n" +
                "\n" +
                "  }\n" +
                "\n" +


                "var filename = 'test.txt';\n" +
                "var fs = require('fs');\n" +
                "if(!fs.isFile(filename)) {\n" +
                "   fs.write(filename, '', 'w');\n" +
                "}\n" +


                "  page.onResourceRequested = function (req) {\n" +
                "      count += 1;\n" +
                "      console.log('> ' + req.id + ' - ' + req.url);\n" +
                "      var content = fs.read(filename);\n" +
                "      fs.write(filename, content + 'requested: ' + req.url + ';', 'w');\n" +
                "      clearTimeout(renderTimeout);\n" +
                "  };\n" +
                "\n" +
                "  page.onResourceReceived = function (res) {\n" +
                "      if (!res.stage || res.stage === 'end') {\n" +
                "          count -= 1;\n" +
                "          console.log(res.id + ' ' + res.status + ' - ' + res.url);\n" +
                "          var content = fs.read(filename);\n" +
                "          fs.write(filename, content + 'received: ' + res.url + ';', 'w');\n" +
                "          if (count === 0) {\n" +
                "              renderTimeout = setTimeout(doRender, resourceWait);\n" +
                "          }\n" +
                "      }\n" +
                "  };");

        phantom.get("file:///home/felix/trackthetrackers/extraction/src/test/resources/zalando.de.html");

        return null;
    }
    */

    //without waiting
    public Iterable<Resource> extractResources(String sourceUrl, String html) {

        File temp = null;
        try{

            //create a temporary html source file
            temp = File.createTempFile(sourceUrl, ".html");

            //write it
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(html);
            bw.close();

        }catch(IOException e){
            e.printStackTrace();
        }


        Capabilities capabilities = new DesiredCapabilities().phantomjs();
        // Set PhantomJS Path
        ((DesiredCapabilities) capabilities).setCapability("phantomjs.binary.path", "/home/felix/Software/phantomjs/bin/phantomjs");
        ((DesiredCapabilities) capabilities).setCapability("phantomjs.page.settings.loadImages", false);

        WebDriver d = new PhantomJSDriver(capabilities);


        if (!(d instanceof PhantomJSDriver)) {
            // Skip this test if not using PhantomJS.
            // The command under test is only available when using PhantomJS
            return null;
        }


        String tempLogFilename = "test.txt";
        File tempLog = null;
        try {
            tempLog = File.createTempFile("log", ".log");
        } catch (IOException e) {
            e.printStackTrace();
        }

        PhantomJSDriver phantom = (PhantomJSDriver) d;


        Object result = phantom.executePhantomJS(
                "var page          = this;\n" +

                "var filename = '" + tempLog.getAbsolutePath() + "';\n" +
                "var fs = require('fs');\n" +

                "page.onResourceRequested = function (req) {\n" +
                "      var content = fs.read(filename);\n" +
                "      fs.write(filename, content + '>' + req.url + ' ', 'w');\n" +
                "};\n" +
                "\n" +
           /*     "page.onResourceReceived = function (res) {\n" +
                "      if (!res.stage || res.stage === 'end') {\n" +
                "          var content = fs.read(filename);\n" +
                "          fs.write(filename, content + res.url + ' ', 'w');\n" +
                "      }\n" +
                "};" */
                "");

        /*
        Object result = phantom.executePhantomJS("var resourceWait  = 300,\n" +
                "      maxRenderWait = 10000;\n" +
                "\n" +
                "  var page          = this,\n" +
                "      count         = 0,\n" +
                "      forcedRenderTimeout,\n" +
                "      renderTimeout;\n" +
                "\n" +
                "  page.viewportSize = { width: 1280,  height : 1024 };\n" +
                "\n" +
                "  function doRender() {\n" +
                "\n" +
                "  }\n" +
                "\n" +


                "var filename = '" + tempLog.getAbsolutePath() + "';\n" +
                "var fs = require('fs');\n" +


                "  page.onResourceRequested = function (req) {\n" +
                "      count += 1;\n" +
                "      console.log('> ' + req.id + ' - ' + req.url);\n" +
                "      var content = fs.read(filename);\n" +
                "      fs.write(filename, content + '>' + req.url + ' ', 'w');\n" +
                "      clearTimeout(renderTimeout);\n" +
                "  };\n" +
                "\n" +
                "  page.onResourceReceived = function (res) {\n" +
                "      if (!res.stage || res.stage === 'end') {\n" +
                "          count -= 1;\n" +
                "          console.log(res.id + ' ' + res.status + ' - ' + res.url);\n" +
                "          var content = fs.read(filename);\n" +
                "          fs.write(filename, content + res.url + ' ', 'w');\n" +
                "          if (count === 0) {\n" +
                "              renderTimeout = setTimeout(doRender, resourceWait);\n" +
                "          }\n" +
                "      }\n" +
                "  };");*/

        phantom.get("file://" + temp.getAbsolutePath());


        Set<Resource> resources = Sets.newHashSet();
        String prefixForInternalLinks = urlNormalizer.createPrefixForInternalLinks(sourceUrl);

        try {
            Scanner scanner = new Scanner(tempLog);
            while (scanner.hasNextLine()) {
                String[] tokens = scanner.nextLine().split(" ");
                //do what you want to do with the tokens

                for (String uri : tokens) {
                    if(uri.startsWith(">")) {
                        uri = uri.substring(1);
                        System.out.println("requested: " + uri);
                    }
                    if (uri.contains(".")) {
                        if(uri.startsWith("file://")) {
                            uri = uri.substring(7);
                            uri = "http://" + uri;
                        }
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
                            resources.add(new Resource(uri, Resource.Type.SCRIPT));
                        }
                    }
                }
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }

        temp.delete(); //delete temporary html source file
        tempLog.delete();//delete temporary request log file

        return resources;
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

}
