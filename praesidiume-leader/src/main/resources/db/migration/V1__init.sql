CREATE TABLE IF NOT EXISTS test
(
  id   UUID PRIMARY KEY,
  name TEXT,
  file OID
);

CREATE TABLE package
(
    id   UUID PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE package_version_description(
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    version TEXT NOT NULL,
    parameters TEXT NOT NULL,
    dependencies TEXT NOT NULL,
    fk_package UUID REFERENCES package ON DELETE CASCADE
);

CREATE TABLE package_version_artifact(
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    version TEXT NOT NULL,
    integrity TEXT NOT NULL,
    parameters TEXT NOT NULL,
    dependencies TEXT NOT NULL,
    content oid NOT NULL,
    fk_package_version UUID REFERENCES package_version_description ON DELETE CASCADE
);
