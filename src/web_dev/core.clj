(ns web-dev.core
  (:use net.cgrand.moustache
	ring.util.response
	ring.adapter.jetty
        [ring.middleware params file]
	net.cgrand.enlive-html)
  )

(deftemplate html-template "test.html" [headline titles]
  [:h1] (content headline)
  [:h2.title] (clone-for [title titles]
			 this-node (content title)))

;; Extract title from Hacker News
;; Matches this:
;; <td class="title"><a href="http://www.erasmatazz.com/Personal/page70/page453/Sixty.html">Chris Crawford on Mortality</a><span class="comhead"> (erasmatazz.com) </span></td></tr
(defn slurp-hn []
  (-> "http://news.ycombinator.com"
      java.net.URL.
      html-resource
      (select [:td.title :a content])))

(def wroutes
     (app
      ;; "/"
      [""]
      (-> "Hello World"
	  response
	  constantly)

      ;; /hello/name 
      ["hello" name]
      (-> (str "Hello, " name)
	  response
	  constantly)

      ;; /enlive/
      ["enlive"]
      (-> (html-template (str (java.util.Date.)) (slurp-hn))
	  response
	  constantly)
      
      ;; Match everything else:
      [&]
      (fn [r] (-> r str response))
      ))

(defn start-server []
  (doto (Thread. #(run-jetty #'wroutes {:port 8080 :host "127.0.0.1"}))
    .start))
