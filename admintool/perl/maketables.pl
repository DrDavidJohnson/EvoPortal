# Copyright 2009 David Johnson, School of Biological Sciences,
# University of Reading, UK.
# 
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

#!/usr/bin/perl

use strict;
use warnings;
my $dbname = 'evoportal'; # name of the database on the database server
my $pbsuser = 'phyloportal'; # username to access the PBS host
my $pbshost = 'sword'; # host running PBS, should already be set up for passwordless login
my $remoteroot = '/home/phyloportal/files/';
print "This script creates the EvoPortal tables for a database named \'$dbname\'\n";
print "on a MySQL server running on the localhost. MySQL may prompt you for a user \n";
print "password...\n";
print "Are you sure you want to create the DB tables (yes/no)?  ";
my $confirm;
$confirm = <STDIN>;
chomp($confirm);

# If the core DB tables in EvoPortal change (i.e. developed further), these SQL scripts will 
# need to be updated so that the portal tables can be created correctly
my $sqlmaketable_PortalUser = '
CREATE TABLE PortalUser
(
user_id BIGINT NOT NULL AUTO_INCREMENT,
username VARCHAR(255),
passwordHash VARCHAR(255),
emailAddress VARCHAR(255),
lastTouch BIGINT,
PRIMARY KEY (user_id)
);';

my $sqlmaketable_ComputeJob = '
CREATE TABLE ComputeJob
(
computejob_id BIGINT NOT NULL AUTO_INCREMENT,
jobID INTEGER,
label VARCHAR(255),
nodes INTEGER,
submitTime BIGINT,
status CHAR(1),
timeRequested BIGINT,
timeUsed BIGINT,
owner VARCHAR(255),
notified BIT,
PRIMARY KEY (computejob_id)
);';

my $sqlmaketable_ScaleTest = '
CREATE TABLE ScaleTest
(
scaletest_id BIGINT NOT NULL AUTO_INCREMENT,
testID BIGINT,
label VARCHAR(255),
BPBlock VARCHAR(255),
owner VARCHAR(255),
iterations INTEGER,
notified BIT,
PRIMARY KEY (scaletest_id)
);';

my $sqlmaketable_ScaleTestComputeJob = '
CREATE TABLE ScaleTestComputeJob
(
scaletestcomputejob_id BIGINT NOT NULL AUTO_INCREMENT,
jobID INTEGER,
label VARCHAR(255),
nodes INTEGER,
submitTime BIGINT,
status CHAR(1),
timeRequested BIGINT,
timeUsed BIGINT,
owner VARCHAR(255),
duration INTEGER,
scaletest_id BIGINT,
indx INTEGER,
PRIMARY KEY (scaletestcomputejob_id),
FOREIGN KEY (scaletest_id) REFERENCES ScaleTest(scaletest_id)
);';

# this statement inserts a user called 'admin' with the password 'adminadmin' (hash generated with PasswordHasher)
my $sqlinsert_admin = '
INSERT INTO PortalUser (username, passwordHash, emailAddress, lastTouch) VALUES (
\'admin\',
\'DD9470EFBFBD28EFBFBD1CEFBFBDD08F30EFBFBDEFBFBD043F4742EFBFBD1F4F\',
\'admin@mydomain.org\',
-1
);';

my $sqlmaketables_all = "$sqlmaketable_PortalUser $sqlmaketable_ComputeJob $sqlmaketable_ScaleTest $sqlmaketable_ScaleTestComputeJob $sqlinsert_admin"; 
if ($confirm eq "yes") {
	print "Creating DB tables\n";
	system "mysql -p $dbname -e \"$sqlmaketables_all\"";
	print "Creating admin filesystem on $pbshost\n";
	system "ssh $pbsuser\@$pbshost mkdir $remoteroot\/admin";
	system "ssh $pbsuser\@$pbshost mkdir $remoteroot\/admin\/mynexusfiles";
} else {
	print "OK, lets do nowt then...\n";
}
