# log-scan

Receives a log file input and prints XML containing relevant information about log file.

## Usage

The file path input should be as the example bellow:
/home/temp/server.xml

The file destination input should be as the example bellow:
/home/temp/

It will generate an XML file following this pattern:
```xml
<report>
  <rendering>
    <!-- Document id -->
    <document>1234</document>
    <page>0</page>
    <!-- UID of the startRendering -->
    <uid>12345665456-4565</uid>
    <!-- One or more timestamps of the startRendering -->
    <start>2010-10-06 09:03:05,873</start>
    <start>2010-10-06 09:03:06,547</start>
    ... (maybe some more starts)
    <!-- One or more timestamps of getRendering -->
    <get>2010-10-06 09:03:05,873</get>
    <get>2010-10-06 09:03:06,547</get>
      ... (Possibly more gets)
  </rendering>
    <!-- Some more renderings... -->
      ...
  <!-- Summary -->
  <summary>
    <!-- Total number of renderings -->
    <count>40</count>
    <!-- Number of double renderings (multiple starts with same UID) -->
    <duplicates>1</duplicates>
    <!-- Number of startRenderings without get -->
    <unnecessary>0</unnecessary>
  </summary>
</report>
```

## License

Copyright © 2020

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.
