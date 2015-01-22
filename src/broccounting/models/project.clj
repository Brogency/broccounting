(ns broccounting.models.project)

(def xml->project_ids (partial map (comp :id :attrs)))
