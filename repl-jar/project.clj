(defproject repl-jar "0.1.0-SNAPSHOT"
  :description "Clojure Web REPL"
  :dependencies 
  	[[org.clojure/clojure "1.7.0"]
  	 [compojure "1.4.0"]
  	 [ring/ring-defaults "0.1.5"]
  	 [ring/ring-core "1.4.0"]
  	 [ring/ring-devel "1.4.0"]
  	 [clj-http "2.0.0"]
  	 [ring/ring-jetty-adapter "1.4.0"]
  	 [org.clojure/data.json "0.2.6"]]
  :main repl.ApplicationREPL
  :aot :all
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
