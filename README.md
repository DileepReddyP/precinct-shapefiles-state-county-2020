### State-wise and county-wise precinct maps

This repository contains the data from NYT Upshot's [Presidential precinct data for the 2020 general election](https://github.com/TheUpshot/presidential-precinct-map-2020) split into individual GeoJSON files by state and individual county(-equivalent region), as well as the Clojure file I used to split the data.

Each state/county GeoJSON file contains the precinct shapefiles for that geographic region.

The MIT license for NYT data is in the `NYT_LICENSE` file. (The NYT data itself is not included)

The files are in the `statewise` directory with the following structure

statewise/  
├── {STATE CODE}/  
│   ├── {STATE CODE}.geojson  
│   └── js/  
│       ├── {COUNTY NAME}.geojson  
│       ...  
|         
...

Note: I have not verified that I have properly split the data for all 3000+ counties.
