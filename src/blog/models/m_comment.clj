(ns blog.models.m-comment
  (:use blog.system.db
	[blog.system.utils :rename {parse-int pint}])
  (:require [clojure.contrib.sql :as sql]))

(defn fetch-list [post-id]
  (doall
   (map struct-map->soy
	(doall
	 (sql/with-connection *db*
	   (sql/with-query-results comment
	     ["SELECT * FROM comment where post_id = ? order by id desc" (pint post-id)]
	     (doall comment)))))))

(defn create [comment]
  (sql/with-connection *db*
    (sql/insert-values "comment" ["title" "body" "post_id"]
		       [(:title comment)
			(:text comment)
			(pint (:post_id comment))])))

(defn fetch [post-id id]
  (struct-map->soy
   (sql/with-connection *db*
     (sql/with-query-results comment
       ["SELECT * FROM comment where id = ? and post_id = ?" (pint id) (pint post-id)]
       (first (doall comment))))))

(defn update [comment]
  (sql/with-connection *db*
    (sql/update-values "comment" ["id = ?" (pint (:id comment))] comment)))

(defn delete [id]
  (sql/with-connection *db*
    (sql/delete-rows "comment" ["id = ?" (pint id)])))
