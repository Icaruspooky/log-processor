package br.com.log.processor;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ReportGenerator {
    private static final String START_RENDERING = "Executing request startRendering";
    private final Processor processor;
    private final Document report;

    public ReportGenerator(Processor processor) throws ParserConfigurationException {
        this.processor = processor;

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        this.report = documentBuilder.newDocument();
    }

    public void generateReport() throws TransformerException {
        Map<Object, List<List<String>>> renders = processor.getRenders();
        List<String> threadNames = processor.getThreadNames();

        Element root = report.createElement("report");
        report.appendChild(root);

        for (String thread : threadNames) {
            for (List<String> render : renders.get(thread)) {
                String renderText = render.toString();
                if (renderText.contains(START_RENDERING)){
                    Element rendering = report.createElement("rendering");
                    root.appendChild(rendering);

                    getDocumentTag(renderText, rendering);
                    getPageTag(renderText, rendering);
                    String uid = getUid(renderText);
                    getUidTag(rendering, uid);
                    getStarts(threadNames, renders, uid, rendering);
                    getGets(threadNames, renders, uid, rendering);
                }
            }
        }

        Element summary = report.createElement("summary");
        root.appendChild(summary);

        getCountTag(summary);
        getDuplicatesTag(summary);
        getUnnecessaryTag(summary);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(report);
        System.out.println("Input the output file location:");
        Scanner scanner = new Scanner(System.in);
        String outputLocation = scanner.nextLine();
        StreamResult streamResult = new StreamResult(new File(outputLocation + "/output.xml"));
        transformer.transform(domSource, streamResult);

        System.out.println("Thanks for reviewing my application!");
    }

    private void getDocumentTag(String renderText, Element rendering) {
        Element documentTag = report.createElement("document");
        Text documentText =  report.createTextNode(StringUtils.substringBefore(StringUtils.substringAfter(StringUtils.substringAfter(renderText, START_RENDERING), "with arguments ["), ","));
        documentTag.appendChild(documentText);
        rendering.appendChild(documentTag);
    }

    private void getPageTag(String renderText, Element rendering) {
        Element pageTag = report.createElement("page");
        Text pageValue =  report.createTextNode(StringUtils.substringBefore(StringUtils.substringAfter(StringUtils.substringAfter(renderText, START_RENDERING), ", "), "] "));
        pageTag.appendChild(pageValue);
        rendering.appendChild(pageTag);
    }

    private String getUid(String renderText) {
        return StringUtils.substringBefore(StringUtils.substringAfter(StringUtils.substringAfter(renderText, START_RENDERING), "{ RenderingCommand - uid: "), " }");
    }

    private void getUidTag(Element rendering, String uid) {
        Element uidTag = report.createElement("uid");
        Text uidValue =  report.createTextNode(uid);
        uidTag.appendChild(uidValue);
        rendering.appendChild(uidTag);
    }

    private void getStarts(List<String> threadNames, Map<Object, List<List<String>>> renders, String uid, Element rendering) {
        for (String td : threadNames) {
            for (List<String> rend : renders.get(td)) {
                String rendText = rend.toString();
                String rendUid = getUid(rendText);
                for (String line : rend) {
                    if (line.contains(START_RENDERING) && uid.equals(rendUid)) {
                        Element startTag = report.createElement("start");
                        Text startText =  report.createTextNode(StringUtils.substringBefore(line, " ["));
                        startTag.appendChild(startText);
                        rendering.appendChild(startTag);
                    }
                }
            }
        }
    }

    private void getGets(List<String> threadNames, Map<Object, List<List<String>>> renders, String uid, Element rendering) {
        for (String td : threadNames) {
            for (List<String> rend : renders.get(td)) {
                String rendText = rend.toString();
                String rendUid = StringUtils.substringBefore((StringUtils.substringAfter(rendText, "Executing request getRendering with arguments [")), "]");
                for (String line : rend) {
                    if (line.contains("Executing request getRendering with arguments [") && uid.equals(rendUid)) {
                        Element startTag = report.createElement("get");
                        Text startText =  report.createTextNode(StringUtils.substringBefore(line, " ["));
                        startTag.appendChild(startText);
                        rendering.appendChild(startTag);
                    }
                }
            }
        }
    }

    private void getCountTag(Element summary) {
        Element countTag = report.createElement("count");
        Text countValue =  report.createTextNode(String.valueOf(processor.getTotalRenderings()));
        countTag.appendChild(countValue);
        summary.appendChild(countTag);
    }

    private void getDuplicatesTag(Element summary) {
        Element duplicatesTag = report.createElement("duplicates");
        Text duplicatesValue =  report.createTextNode(String.valueOf(processor.getDuplicates()));
        duplicatesTag.appendChild(duplicatesValue);
        summary.appendChild(duplicatesTag);
    }

    private void getUnnecessaryTag(Element summary) {
        Element unnecessaryTag = report.createElement("unnecessary");
        Text unnecessaryValue =  report.createTextNode(String.valueOf(processor.getUnnecessary()));
        unnecessaryTag.appendChild(unnecessaryValue);
        summary.appendChild(unnecessaryTag);
    }
}
