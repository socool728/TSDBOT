CREATE TABLE OdbItem (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  item VARCHAR(255) NOT NULL
) ENGINE=InnoDB, CHARSET=utf8;

CREATE TABLE OdbTag (
  itemId VARCHAR(36) NOT NULL,
  tag VARCHAR(255) NOT NULL,
  CONSTRAINT `fk_tag_item` FOREIGN KEY (itemId) REFERENCES OdbItem(id)
) ENGINE=InnoDB, CHARSET=utf8;

CREATE TABLE TSDTVAgent (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  agentId VARCHAR(100) NOT NULL,
  status ENUM('unregistered', 'registered', 'blacklisted') NOT NULL DEFAULT 'unregistered',
  lastHeartbeatFrom VARCHAR(100),
  CONSTRAINT UNIQUE (agentId),
  INDEX (agentId),
  INDEX (status)
) ENGINE=InnoDB, CHARSET=utf8;

CREATE TABLE TSDTVEpisodicItem (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  seriesName VARCHAR(255) NOT NULL,
  seasonName VARCHAR(255),
  currentEpisode INT UNSIGNED NOT NULL DEFAULT 1,
  CONSTRAINT UNIQUE (seriesName, seasonName),
  INDEX (seriesName),
  INDEX (seasonName)
) ENGINE=InnoDB, CHARSET=utf8;

CREATE TABLE User (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  passwordHash VARCHAR(255) NOT NULL,
  emailAddress VARCHAR(150),
  role ENUM('peon', 'staff', 'admin') NOT NULL DEFAULT 'peon',
  lastLoggedInTime TIMESTAMP,
  lastLoggedInFrom VARCHAR(100),
  CONSTRAINT UNIQUE (username),
  CONSTRAINT UNIQUE (emailAddress),
  INDEX (username),
  INDEX (emailAddress),
  INDEX (role),
  INDEX (lastLoggedInTime)
) ENGINE=InnoDB, CHARSET=utf8;

CREATE TABLE NewsTopic (
  userId VARCHAR(255) NOT NULL,
  topic VARCHAR(255) NOT NULL,
  CONSTRAINT UNIQUE (userId, topic),
  INDEX (userId)
) ENGINE=InnoDB, CHARSET=utf8;