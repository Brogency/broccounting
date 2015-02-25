(ns broccounting.models.elba)

(defn credentials [session]
  (:credentials session [nil nil]))

(defn update-credentials [session login password]
  (assoc-in session [:credentials] [login password]))
