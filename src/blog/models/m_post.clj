(ns blog.models.m-post
  (:use [blog.system.utils :rename {parse-int pint}])
  (:require [clojure.contrib.sql :as sql]))

(defn fetch-list []
  (sql/with-query-results posts
    ["SELECT * FROM post order by id desc"]
    (or (doall posts) '())))

(defn create [post]
  (sql/insert-values "post" ["title" "body"]
                     [(:title post)
                      (:text post)]))

(defn fetch [id]
  (sql/with-query-results post
    ["SELECT * FROM post where id = ?" (pint id)]
    (first post)))

(defn update [post]
  (sql/update-values "post" ["id = ?" (pint (:id post))]
                     (select-keys post [:title :body])))

(defn delete [id]
  (sql/delete-rows "post" ["id = ?" (pint id)]))
