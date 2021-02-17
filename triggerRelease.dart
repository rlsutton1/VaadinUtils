#! /usr/bin/env dcli
import 'dart:io';

import 'package:dshell/dshell.dart';

/// comment

void main() {
  int major;
  int minor;
  int rev = 0;
  var dir = dirname(Settings().scriptPath);
  print(dir);
  read(join(dir, "pom.xml")).forEach((line) {
    if (line.contains("<releaseVersion>")) {
      var parts = line.split(".");
      if (parts.length != 3) {
        exit(1);
      }
      major = int.parse(parts[0].split(">")[1]);
      minor = int.parse(parts[1]);
      rev = int.parse(parts[2].split("<")[0]);
    }
  });
  rev++;

  String version = "$major.$minor.$rev";

  replace(join(dir, "pom.xml"), version);
  'mvn -f ${join(dir, "pom.xml")} -DskipTests=true deploy'.run;
  'git pull'.run;
  'git add .'.run;
  'git commit -m "for version $version"'.run;
  'git tag -a $version -m "$version"'.run;
  'git push origin tag $version'.run;
  'git pull'.run;
  'git push origin'.run;
}

void replace(String path, version) {
  var tmp = '$path.tmp';
  if (exists(tmp)) {
    delete(tmp);
  }
  read(path).forEach((line) {
    if (line.contains("<releaseVersion>")) {
      line = "        <releaseVersion>$version</releaseVersion>";
    }
    tmp.append(line);
  });
  move(path, '$path.bak');
  move(tmp, path);
  delete('$path.bak');
}
