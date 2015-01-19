/**
 * Track the trackers
 * Copyright (C) 2014  Sebastian Schelter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.ssc.trackthetrackers.extraction.hadoop;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import io.ssc.trackthetrackers.extraction.hadoop.io.ArcInputFormat;
import io.ssc.trackthetrackers.extraction.hadoop.io.ArcRecord;
import io.ssc.trackthetrackers.extraction.resources.GoogleParserAllExtractor;
import io.ssc.trackthetrackers.extraction.resources.GoogleParserExtractor;
import io.ssc.trackthetrackers.extraction.resources.RegexResourceExtractor;
import io.ssc.trackthetrackers.extraction.resources.Resource;
import io.ssc.trackthetrackers.commons.proto.ParsedPageProtos;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Job;

import com.google.common.collect.Sets;

import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.entity.ContentType;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import parquet.hadoop.metadata.CompressionCodecName;
import parquet.proto.ProtoParquetOutputFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class ExtractionJob extends HadoopJob {

  public static enum Counters {
    PAGES, RESOURCES
  }

  @Override
  public int run(String[] args) throws Exception {

    Map<String,String> parsedArgs = parseArgs(args);

    Path inputPath = new Path(parsedArgs.get("--input"));
    Path outputPath = new Path(parsedArgs.get("--output"));

    Job job = mapOnly(inputPath, outputPath, ArcInputFormat.class, ProtoParquetOutputFormat.class,
                      CommonCrawlExtractionMapper.class, null, null, true);

    ProtoParquetOutputFormat.setProtobufClass(job, ParsedPageProtos.ParsedPage.class);
    ProtoParquetOutputFormat.setCompression(job, CompressionCodecName.SNAPPY);
    ProtoParquetOutputFormat.setEnableDictionary(job, true);

    job.waitForCompletion(true);

    return 0;
  }

  static class CommonCrawlExtractionMapper extends Mapper<Writable, ArcRecord, Void, ParsedPageProtos.ParsedPage> {

    //private final RegexResourceExtractor resourceExtractor = new RegexResourceExtractor();
    //private final GhostDriverExtractor resourceExtractor = new GhostDriverExtractor();
    private final GoogleParserExtractor resourceExtractor = new GoogleParserExtractor();
    private final GoogleParserAllExtractor resourceExtractor1 = new GoogleParserAllExtractor();

    //private  PhantomJSDriver p = GhostDriverExtractor.setup();

    @Override
    public void map(Writable key, ArcRecord record, Context context) throws IOException, InterruptedException {

      if ("text/html".equals(record.getContentType())) {
        //System.out.println(record.getURL());

        String charset = null;

        try {
          HttpResponse httpResponse = record.getHttpResponse();
          // Default value returned is "html/plain" with charset of ISO-8859-1.
          try {
            charset = ContentType.getOrDefault(httpResponse.getEntity()).getCharset().name();
          } catch (Exception e) {
            // TODO have a counter for this
          }

          // if anything goes wrong, try ISO-8859-1
          if (charset == null) {
            charset = "ISO-8859-1";
          }

          String html;
          InputStreamReader reader = new InputStreamReader(httpResponse.getEntity().getContent(), charset);
          try {
            html = CharStreams.toString(reader);
          }
          finally {
            Closeables.close(reader, true);
          }

          //Iterable<Resource> resources = resourceExtractor.extractResources(record.getURL(), html,p);

          Iterable<Resource> resources = resourceExtractor.extractResources(record.getURL(), html);

          Iterable<Resource> resources1 = resourceExtractor1.extractResources(record.getURL(), html);


          boolean goad = false;
          for(Resource r : resources1) {
            if (r.url().contains("connect.facebook")) {
              goad = true;
              break;
            }
          }
          if(goad) {
            boolean debuggoad=false;
            for(Resource dr : resources) {
              if (dr.url().contains("connect.facebook")) {
                debuggoad = true;
                break;
              }
            }

            if(debuggoad == false && !html.contains("gawker_getScript(") && !html.contains("$.getScript(")
                    && !html.contains("Asset.javascript") && !html.contains("asyncLoad(")
                    && !html.contains("$(document.body).append") && !html.contains("topix_loadScript(")
                    && !html.contains("RB.loadAsyncScript") && !html.contains("addJavascript(") &&
                    !html.contains("jQuery.extend(")) {

              if(!html.contains("appId: '132725426770373'")) {
                System.out.println("html:\n" + html + "length: " + html.length());

                System.exit(1);
              }
            }
          }


          context.getCounter(Counters.PAGES).increment(1);
          context.getCounter(Counters.RESOURCES).increment(Iterables.size(resources));

          ParsedPageProtos.ParsedPage.Builder builder = ParsedPageProtos.ParsedPage.newBuilder();

          builder.setUrl(record.getURL())
                 .setArchiveTime(record.getArchiveDate().getTime());

          for (Resource resource : resources) {
            if (Resource.Type.SCRIPT.equals(resource.type())) {
              builder.addScripts(resource.url());
            } else if (Resource.Type.IFRAME.equals(resource.type())) {
              builder.addIframes(resource.url());
            } else if (Resource.Type.LINK.equals(resource.type())) {
              builder.addLinks(resource.url());
            } else if (Resource.Type.IMAGE.equals(resource.type())) {
              builder.addImages(resource.url());
            }
          }

          context.write(null, builder.build());

        } catch (ProtocolException pe) {
          // TODO have a counter for this
        } catch (Exception e) {
          // TODO have a counter for this
          throw new IOException(e);
        }
      }
    }
  }
}
