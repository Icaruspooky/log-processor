package br.com.log.processor;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Processor {
    @Getter private final List<String> lines = new ArrayList<>();
    @Getter private int totalRenderings = 0;
    @Getter private int duplicates = 0;
    @Getter private int unnecessary = 0;
    @Getter private Map<String, Long> uidsFrequency = new HashMap<>();
    @Getter private final List<String> threadNames = new ArrayList<>();
    @Getter private final Map<Object, List<String>> threads = new HashMap<>();
    @Getter private final Map<Object, List<List<String>>> renders = new HashMap<>();

    public Processor(FileInputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = br.readLine()) != null) {
            lines.add(line);
        }

        this.processDocument();
    }

    private void processDocument() {
        Set<String> gets = new LinkedHashSet<>();
        Set<String> uids = new LinkedHashSet<>();
        List<String> uidCounter = new ArrayList<>();

        for(String line : lines) {
            setThreadNames(line);
            setTotalRenderings(line);
            getUid(line, uids, uidCounter);
            getGets(line, gets);
        }

        setUidsFrequency(uidCounter);
        setUnnecessaryRenders(uids, gets);
        setDuplicates(uids);
        setRenders();
    }

    private void getUid(String line, Set<String> uids, List<String> uidCounter) {
        String uid = StringUtils.substringBefore(StringUtils.substringAfter(line, "Service startRendering returned "), " ");
        if (!StringUtils.isBlank(uid)) {
            uids.add(uid);
            uidCounter.add(uid);
        }
    }

    private void getGets(String line, Set<String> gets) {
        String get = StringUtils.substringBefore((StringUtils.substringAfter(line, "Executing request getRendering with arguments [")), "]");
        if (!StringUtils.isBlank(get)) {
            gets.add(get);
        }
    }

    private void setThreadNames(String line) {
        String threadName = StringUtils.substringBefore((StringUtils.substringAfter(line, "[")), "]");
        if(!threadNames.contains(threadName) && StringUtils.startsWith(threadName, "WorkerThread-")){
            threadNames.add(threadName);
            threads.putAll(lines.stream().filter(logLine -> logLine.contains("[" + threadName + "]")).collect(Collectors.groupingBy(logLine -> threadName)));
        }
    }

    private void setTotalRenderings(String line) {
        this.totalRenderings = line.contains("Executing request startRendering") ? totalRenderings + 1 : totalRenderings;
    }

    private void setUnnecessaryRenders(Set<String> uids, Set<String> gets) {
        List<String> auxUids = new ArrayList<>(uids);
        auxUids.removeAll(gets);
        this.unnecessary = auxUids.size();
    }

    private void setUidsFrequency(List<String> uidCounter) {
        this.uidsFrequency = uidCounter.stream().collect(
                Collectors.groupingBy(
                        Function.identity(),
                        HashMap::new,
                        Collectors.counting()));
    }

    private void setDuplicates(Set<String> uids) {
        for (String uid : uids) {
            long frequency = uidsFrequency.get(uid);
            if (frequency > 1) {
                duplicates++;
            }
        }
    }

    private void setRenders() {
        for (String thread : threadNames) {
            List<List<String>> threadArr = new ArrayList<>();
            List<String> arr = new ArrayList<>();
            for (String line : threads.get(thread)) {
                if(line.contains("Service startRendering returned")){
                    arr.add(line);
                    threadArr.add(new ArrayList<>(arr));
                    arr = new ArrayList<>();
                } else {
                    arr.add(line);
                }
            }
            if (!arr.isEmpty()) {
                threadArr.add(new ArrayList<>(arr));
            }
            renders.put(thread, threadArr);
        }
    }
}
