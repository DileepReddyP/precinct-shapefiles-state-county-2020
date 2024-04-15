(ns parse-upshot-data
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def features-only-filepath "./dataset/features-only.geojson")
(def fips-table-filepath "./dataset/fips2county.tsv")
(defn state-geojson-filepath [state-code]
  (str "./statewise/" state-code "/" state-code ".geojson"))
(defn county-geojson-filepath [state-code county-name]
  (str "./statewise/" state-code "/counties/" county-name ".geojson"))
 
(comment
  ;;; run `jq -c '.features' ./dataset/precincts-with-results.geojson > ./dataset/features-only.geojson`
  ;;; to remove only the features array from the giant geojson 
  ;;; so that it can be lazily processed by json/parse-stream
  #_(def nyt-dataset-filepath "./dataset/precincts-with-results.geojson")
  :rcf)

(defn convert-fips-to-map [fips-map row]
  (let [state-fips (-> row first string/trim)
        county-fips (-> row (nth 4) string/trim)
        county-name (-> row (nth 2) string/trim (string/replace #" " "_"))]
    (if (get fips-map state-fips)
      (assoc-in fips-map [state-fips :counties county-fips] {:name county-name})
      (assoc fips-map state-fips {:name (-> row (nth 5) string/trim)
                                  :counties {county-fips {:name county-name}}}))))

(defn make-fips-map [filepath]
  (with-open [r (io/reader filepath)]
  (->> (csv/read-csv r :separator \tab)
       (rest)
       (reduce convert-fips-to-map {}))))

(def fips-map (make-fips-map fips-table-filepath))

(get-in fips-map ["36" :counties "36001"])

(defn make-directories []
  (->> (keys fips-map)
       (map #(get-in fips-map [% :name]))
       (map #(.mkdirs (io/file (str "./statewise/" % "/counties"))))))

(make-directories)

(defn geojson-format [region-name feature-array]
  {:type "FeatureCollection"
   :name (str region-name "_precincts")
   :features feature-array})

(def get-state-fips #(subs (-> % :properties :GEOID) 0 2))

(def get-county-fips #(subs (-> % :properties :GEOID) 0 5))

(def get-state-features #(get-in %1 [%2 :features] false))

(def get-county-features #(get-in %1 [%2 :counties %3 :features] false))

#_(defonce counter (atom 0))
#_(def print-counter (when (zero? (rem @counter 1000)) (println @counter)))

#_(defn split-by-state [filename batchsize]
    (with-open [r (io/reader filename)]
      (doseq [batch (partition-all batchsize (json/parse-stream r keyword))]
        (swap! counter inc)
        (println @counter)
        (doseq [feature batch]
          (state-feature-vec-conj feature)))))



