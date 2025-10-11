(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.edn :as edn]))

(def lib 'io.github.arachtivix/chess-engine-wrapper)
(def version-file "version.edn")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn- read-version []
  (edn/read-string (slurp version-file)))

(defn- version-string [{:keys [major minor patch]}]
  (format "%d.%d.%d" major minor patch))

(defn get-version [_]
  (let [version (read-version)]
    (println (version-string version))))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (let [version (read-version)
        version-str (version-string version)
        jar-file (format "target/%s-%s.jar" (name lib) version-str)]
    (clean nil)
    (b/write-pom {:class-dir class-dir
                  :lib lib
                  :version version-str
                  :basis basis
                  :src-dirs ["src"]
                  :scm {:url "https://github.com/arachtivix/chess-engine-wrapper"
                        :connection "scm:git:git://github.com/arachtivix/chess-engine-wrapper.git"
                        :developerConnection "scm:git:ssh://git@github.com/arachtivix/chess-engine-wrapper.git"
                        :tag (str "v" version-str)}
                  :pom-data [[:description "A Clojure library that wraps UCI chess engines"]
                             [:url "https://github.com/arachtivix/chess-engine-wrapper"]
                             [:licenses
                              [:license
                               [:name "MIT License"]
                               [:url "https://opensource.org/licenses/MIT"]]]
                             [:developers
                              [:developer
                               [:name "arachtivix"]]]]})
    (b/copy-dir {:src-dirs ["src" "resources"]
                 :target-dir class-dir})
    (b/jar {:class-dir class-dir
            :jar-file jar-file})
    (println "Built:" jar-file)
    jar-file))

(defn increment-patch [_]
  (let [version (read-version)
        new-version (update version :patch inc)]
    (spit version-file (pr-str new-version))
    (println "Version incremented to" (version-string new-version))
    new-version))
