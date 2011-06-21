(ns blog.models.m-comment
  (:use [blog.system.utils :rename {parse-int pint}])
  (:require [clojure.contrib.sql :as sql]))

(defn fetch-list [post-id]
  (sql/with-query-results comments
    ["SELECT * FROM comment where post_id = ? order by id desc" (pint post-id)]
    (or (doall comments) '())))

(defn create [comment]
  (sql/insert-values "comment" ["title" "body" "post_id"]
                     [(:title comment)
                      (:text comment)
                      (pint (:post_id comment))]))

(defn fetch [post-id id]
  (sql/with-query-results comment
    ["SELECT * FROM comment where id = ? and post_id = ?" (pint id) (pint post-id)]
    (first comment)))

(defn update [comment]
  (sql/update-values "comment" ["id = ?" (pint (:id comment))] comment))

(defn delete [id]
  (sql/delete-rows "comment" ["id = ?" (pint id)]))
