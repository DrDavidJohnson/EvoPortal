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
my $remoteroot = '/home/phyloportal/files';
print "This script deletes a EvoPortal user in \'$dbname\'\n";
print "on a MySQL server running on the localhost. MySQL may prompt you for a user \n";
print "password...\n";
print "User to delete: ";
my $username = <STDIN>;
chomp($username);
print "Are you sure you want to delete this user (yes/no)?: ";
my $confirm = <STDIN>;
chomp($confirm);
my $sqldelete_user = "DELETE FROM PortalUser WHERE username=\'$username\';";
if ($confirm eq "yes") {
	print "Deleting $username from database, you may be prompted for the DB user password\n";
	system "mysql -p $dbname -e \"$sqldelete_user\"";
	print "Deleting $username filesystem on $pbshost, you may be prompted for the PBS portal-user password\n";
	system "ssh $pbsuser\@$pbshost rm -r $remoteroot\/$username";
}

#TODO implement rollback if something goes wrong with creating directories
