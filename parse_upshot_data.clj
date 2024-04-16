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
      (assoc-in fips-map [state-fips :counties county-fips] {:name county-name
                                                             :features []})
      (assoc fips-map state-fips {:name (-> row (nth 5) string/trim)
                                  :features []
                                  :counties {county-fips {:name county-name
                                                          :features []}}}))))

(defn make-fips-map [filepath]
  (with-open [r (io/reader filepath)]
    (->> (csv/read-csv r :separator \tab)
         (rest)
         (reduce convert-fips-to-map {}))))

(def fips-map (make-fips-map fips-table-filepath))
(def fips-keys (keys fips-map))

(defn make-directories [fips-map]
  (->> fips-keys
       (map #(get-in fips-map [% :name]))
       (map #(.mkdirs (io/file (str "./statewise/" % "/counties"))))))

(make-directories fips-map)

(defn geojson-format [region-name feature-array]
  {:type "FeatureCollection"
   :name (str region-name "_precincts")
   :features feature-array})

(def get-state-fips #(subs (-> % :properties :GEOID) 0 2))

(def get-county-fips #(subs (-> % :properties :GEOID) 0 5))

#_(def get-state-features #(get-in %1 [%2 :features]))

#_(def get-county-features #(get-in %1 [%2 :counties %3 :features]))

(defonce counter (atom 0))

(defn copy-features-to-map [current-state-fips state-map feature]
  (let [state-fips (get-state-fips feature)
        county-fips (get-county-fips feature)]
    (if (= state-fips current-state-fips)
      (update-in (update state-map :features conj feature) [:counties county-fips :features] conj feature)
      state-map)))

(defn save-map-to-files [state-map]
  (let [state-code (:name state-map)]
    (with-open [w (io/writer (state-geojson-filepath state-code))]
      (json/generate-stream (geojson-format state-code (:features state-map)) w))
    (doseq [county (vals (:counties state-map))] 
      (let [county-name (:name county)]
        (with-open [w (io/writer (county-geojson-filepath state-code county-name))]
          (json/generate-stream (geojson-format county-name (:features county)) w))))))

(defn split-by-state [filename state-fips]
  (with-open [r (io/reader filename)]
    (swap! counter inc)
    (println @counter ":" (get-in fips-map [state-fips :name]))
    (as-> (json/parse-stream r keyword) $
      (reduce (partial copy-features-to-map state-fips) (get fips-map state-fips) $)
      (save-map-to-files $))))

(doseq [state-fips fips-keys]
  (split-by-state features-only-filepath state-fips))




