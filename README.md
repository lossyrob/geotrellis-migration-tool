# [GeoTrellis](github.com/geotrellis/geotrellis) migration tool

This tool to provide legacy metadata migration up to RC1 / RC2 [GeoTrellis](github.com/geotrellis/geotrellis) versions.

## Build

To build you can just run 

```bash
./sbt assembly
```

## Args

```bash
geotrellis-migration-tool 0.1.0-SNAPSHOT
Usage: geotrellis-migration-tool [options]

  --index-type <value>
        index-type is a String property, available index types: zorder, hilbert, rowmajor
  --tile-type <value>
        tile-type is a String property, available index types: singleband, multiband
  --backend <value>
        backend is a String property, available backend types: hadoop, file, s3, accumulo
  --x-resolution <value>
        x-resolution is an Int property
  --y-resolution <value>
        y-resolution is an Int property
  --temporal-resolution <value>
        temporal-resolution is a Long property
  --istance-name <value>
        instance-name is a non-empty String property
  --zookeeper <value>
        zookeeper is a non-empty String property
  --user <value>
        user is a non-empty String property
  --pwd <value>
        pwd is a non-empty String property
  --table <value>
        table is a non-empty String property
  --file-path <value>
        file-path is a non-empty String property
  --hadoop-path <value>
        hadoop-path is a non-empty String property
  --hadoop-defaultFS <value>
        hadoop-defaultFS is a non-empty String property
  --bucket <value>
        bucket is a non-empty String property
  --prefix <value>
        prefix is a non-empty String property
  --help
        prints this usage text
```

## License

* Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
