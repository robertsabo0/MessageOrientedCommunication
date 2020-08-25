CREATE TABLE `test_params` (
  `testNo` int(11) NOT NULL AUTO_INCREMENT,
  `testDurationSeconds` int(11) NOT NULL DEFAULT 10,
  `everyXms` int(11) NOT NULL DEFAULT 500,
  `sendYmessages` int(11) NOT NULL DEFAULT 1,
  `ofZsize` int(11) NOT NULL DEFAULT 10,
  `paralelOnTThreads` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`testNo`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;

CREATE TABLE `test_run` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commType` varchar(45) NOT NULL,
  `testParamsId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `testRunToParams_idx` (`testParamsId`),
  CONSTRAINT `testRunToParam` FOREIGN KEY (`testParamsId`) REFERENCES `test_params` (`testNo`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=latin1;

CREATE TABLE `test_result` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `testRunId` int(11) NOT NULL,
  `tsFromStart` int(11) NOT NULL,
  `responseTime` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `testResultToRUn_idx` (`testRunId`),
  CONSTRAINT `testResultToRUn` FOREIGN KEY (`testRunId`) REFERENCES `test_run` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=339 DEFAULT CHARSET=latin1;
