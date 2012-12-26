CREATE SCHEMA "aid" AUTHORIZATION aid;	

--ALTER ROLE aid IN DATABASE aid SET search_path = aid;		

SET "search_path" TO "aid";

CREATE OR REPLACE FUNCTION update_changetimestamp_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.timestamp = now(); 
   RETURN NEW;
END;
$$ language 'plpgsql';

-- Dumping structure for table aid.block
CREATE TABLE IF NOT EXISTS "block" (
  "id" varchar(64) PRIMARY KEY UNIQUE NOT NULL
);
COMMENT ON TABLE "block" IS 'Blocked Items. Programm will tag files.';

-- Data exporting was unselected.


-- Dumping structure for table aid.cache
CREATE TABLE IF NOT EXISTS "cache" (
  "id" varchar(48) PRIMARY KEY UNIQUE NOT NULL,
  "timestamp" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE "cache" IS 'Recently downloaded files';

CREATE TRIGGER update_cache_changetimestamp BEFORE UPDATE
    ON "cache" FOR EACH ROW EXECUTE PROCEDURE 
    update_changetimestamp_column();
-- Data exporting was unselected.


-- Dumping structure for table aid.dirlist
CREATE TABLE IF NOT EXISTS "dirlist" (
  "id" serial NOT NULL PRIMARY KEY,
  "dirpath" varchar(255) UNIQUE NOT NULL
);
CREATE INDEX "dirpath" ON "dirlist" ("dirpath");
COMMENT ON TABLE "dirlist" IS 'List of all known directories';

-- Data exporting was unselected.


-- Dumping structure for table aid.dnw
CREATE TABLE IF NOT EXISTS "dnw" (
  "id" varchar(64) PRIMARY KEY UNIQUE NOT NULL
);
COMMENT ON TABLE "dnw" IS 'Unwanted files';

-- Data exporting was unselected.


-- Dumping structure for view aid.dupeview
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE "dupeview" (
	"id" VARCHAR(64) NOT NULL,
	"dupeloc" VARCHAR(30) NOT NULL,
	"dupePath" VARCHAR(510) NULL DEFAULT NULL,
	"origloc" VARCHAR(30) NOT NULL,
	"origPath" VARCHAR(510) NULL DEFAULT NULL
);

-- Dumping structure for table aid.filelist
CREATE TABLE IF NOT EXISTS "filelist" (
  "id" serial PRIMARY KEY NOT NULL,
  "filename" varchar(255) UNIQUE NOT NULL
);
COMMENT ON TABLE "filelist" IS 'List of all known filenames';

CREATE INDEX "filename" ON "filelist" ("filename");

-- Dumping structure for table aid.location_tags
CREATE TABLE IF NOT EXISTS "location_tags" (
  "tag_id" serial PRIMARY KEY UNIQUE NOT NULL,
  "location" varchar(30) UNIQUE NOT NULL
);
COMMENT ON TABLE "location_tags" IS 'List of tags for different locations where data is stored';

INSERT INTO "location_tags" ("tag_id", "location") VALUES (1, 'UNKNOWN');
INSERT INTO "location_tags" ("tag_id", "location") VALUES (2, 'ARCHIVE');


-- Data exporting was unselected.
-- Dumping structure for table aid.fileduplicate
CREATE TABLE IF NOT EXISTS "fileduplicate" (
  "id" varchar(64)  NOT NULL,
  "size" int NOT NULL DEFAULT '0',
  "dir" int NOT NULL DEFAULT '0',
  "filename" int NOT NULL DEFAULT '0',
  "location" int NOT NULL,
  PRIMARY KEY ("id","dir","filename"),
  UNIQUE ("id","dir","filename"),
  FOREIGN KEY ("dir") REFERENCES "dirlist" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("filename") REFERENCES "filelist" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("location") REFERENCES "location_tags" ("tag_id")
);
COMMENT ON TABLE "fileduplicate" IS 'List of duplicate files';

-- Data exporting was unselected.


-- Dumping structure for table aid.fileindex
CREATE TABLE IF NOT EXISTS "fileindex" (
  "id" varchar(64)  PRIMARY KEY UNIQUE  NOT NULL,
  "size" int NOT NULL DEFAULT '0',
  "dir" int NOT NULL DEFAULT '0',
  "filename" int NOT NULL DEFAULT '0',
  "location" int NOT NULL,
  FOREIGN KEY ("dir") REFERENCES "dirlist" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("filename") REFERENCES "filelist" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("location") REFERENCES "location_tags" ("tag_id")
);
COMMENT ON TABLE "fileindex" IS 'all known files';

-- Dumping structure for table aid.filter
CREATE TABLE IF NOT EXISTS "filter" (
  "id" varchar(50) PRIMARY KEY UNIQUE NOT NULL,
  "board" varchar(2) NOT NULL,
  "reason" varchar(30) NOT NULL,
  "status" int NOT NULL,
  "timestamp" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE "filter" IS 'filtered threads';

CREATE TRIGGER update_filter_changetimestamp BEFORE UPDATE
    ON "filter" FOR EACH ROW EXECUTE PROCEDURE 
    update_changetimestamp_column();

CREATE TRIGGER insert_filter_changetimestamp BEFORE INSERT
   ON aid.filter FOR EACH ROW
   EXECUTE PROCEDURE aid.update_changetimestamp_column();


-- Dumping structure for view aid.indexview
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE "indexview" (
	"id" VARCHAR(64) NOT NULL,
	"fullpath" VARCHAR(510) NULL DEFAULT NULL,
	"size" int NOT NULL DEFAULT '0',
	"location" VARCHAR(30) NOT NULL
);

CREATE OR REPLACE FUNCTION list_compare()
returns setof record as

$$
select indexview.id, indexview.fullpath, indexview.location from indexview join dnw on indexview.id=dnw.id;
select indexview.id, indexview.fullpath, indexview.location from indexview join block on indexview.id=block.id;
$$

LANGUAGE SQL;



-- Dumping structure for table aid.settings
CREATE TABLE IF NOT EXISTS "settings" (
  "name" varchar(20) UNIQUE NOT NULL,
  "param" text NOT NULL
);
COMMENT ON TABLE "settings" IS 'Global settings for all clients';

INSERT INTO "settings" ("name", "param") VALUES ('SchemaVersion', '3');

-- Dumping structure for table aid.thumbs
CREATE TABLE IF NOT EXISTS thumbs (
  "id" serial PRIMARY KEY NOT NULL,
  "url" varchar(50)  NOT NULL,
  "filename" varchar(25)  NOT NULL,
  "thumb" bytea NOT NULL
);
COMMENT ON TABLE thumbs IS 'Thumbnails for items in the Filter list';

-- Data exporting was unselected.

-- Dumping structure for trigger aid.prune_thumbs_del
CREATE OR REPLACE FUNCTION prune_thumbs_del()
RETURNS TRIGGER AS $$
BEGIN
   DELETE FROM aid.thumbs WHERE url = OLD.id;
   RETURN NEW;
END;
$$ language PLPGSQL;


CREATE TRIGGER prune_thumbs_del AFTER DELETE
    ON "filter" FOR EACH ROW EXECUTE PROCEDURE 
    prune_thumbs_del();
	
-- Dumping structure for trigger aid.prune_thumbs_up

CREATE OR REPLACE FUNCTION prune_thumbs_up()
RETURNS TRIGGER AS $$
BEGIN
	IF NEW.status != 1 THEN
		DELETE FROM aid.thumbs WHERE url = OLD.id;
	END IF;
	RETURN NEW;
END;
$$ language PLPGSQL;

CREATE TRIGGER prune_thumbs_up AFTER UPDATE
    ON "filter" FOR EACH ROW EXECUTE PROCEDURE 
    prune_thumbs_up();
	
-- Dumping structure for view aid.dupeview
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS "dupeview";
CREATE OR REPLACE VIEW "dupeview" AS 
SELECT "fileduplicate".id, dupeloc.location AS dupeloc, CONCAT(dirlist.dirpath, filelist.filename) AS dupePath, origloc.location AS origloc, CONCAT(idir.dirpath, ifile.filename) AS origPath FROM 
	"fileduplicate" JOIN "fileindex" ON "fileduplicate".id = "fileindex".id 
		JOIN filelist ON "fileduplicate".filename=filelist.id
			JOIN dirlist ON "fileduplicate".dir=dirlist.id
				JOIN location_tags AS origloc ON origloc.tag_id=fileindex.location
					JOIN dirlist AS idir ON idir.id = "fileindex".dir 
						JOIN filelist AS ifile ON "fileindex".filename = ifile.id
							JOIN location_tags AS dupeloc ON dupeloc.tag_id=fileduplicate.location ;


-- Dumping structure for view aid.indexview
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS "indexview";
CREATE OR REPLACE VIEW "indexview" AS 
SELECT a.id, CONCAT(dirlist.dirpath,filelist.filename) as fullpath, a.size, location_tags.location FROM "fileindex" as a JOIN filelist ON a.filename=filelist.id JOIN dirlist on a.dir=dirlist.id JOIN location_tags ON a.location = location_tags.tag_id ;

