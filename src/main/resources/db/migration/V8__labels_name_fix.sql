ALTER TABLE labels DROP CONSTRAINT IF EXISTS labels_name_key;
DROP INDEX  IF EXISTS index_unique_label_name;

CREATE UNIQUE INDEX index_unique_label_name
    ON labels (name)
    WHERE deleted_at IS NULL;