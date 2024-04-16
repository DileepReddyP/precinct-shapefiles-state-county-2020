### State-wise and county-wise precinct maps

This repository contains the data from NYT Upshot's [Presidential precinct data for the 2020 general election](https://github.com/TheUpshot/presidential-precinct-map-2020) split into individual GeoJSON files by state and individual county(-equivalent region), as well as the Clojure file I used to split the data.

Each state/county GeoJSON file contains the precinct shapefiles for that geographic region.

The MIT license for NYT data is in the `NYT_LICENSE` file. (The single NYT data file itself is not included)

The FIPS -> State/County mapping is from [Chuck Connell's TSV filtered from an US Census FIPS spreadsheet](https://github.com/ChuckConnell/articles/blob/master/fips2county.tsv)

The files are in the `statewise` directory with the following structure:  

```
statewise/  
├── NY/  
│   ├── NY.geojson  
│   └── counties/  
│       ├── Albany.geojson  
│       ├── Allegany.geojson  
│       ├── Bronx.geojson  
│       ...  
├── AK/  
│   ├── AK.geojson  
│   └── counties/  
│       ├── Aleutians_East_Borough.geojson  
│       ├── Aleutians_West_Census_Area.geojson  
│       ├── Anchorage_Municipality.geojson  
│       ...  
|         
...
```


Note: I have not verified that I have properly split the data for all 3000+ counties.
