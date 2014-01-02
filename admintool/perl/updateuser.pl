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
print "This script updates a EvoPortal user in \'$dbname\'\n";
print "on a MySQL server running on the localhost. MySQL may prompt you for a user \n";
print "password...\n";
print "User to update: ";
my $username = <STDIN>;
chomp($username);
print "Enter the password: ";
my $password = <STDIN>;
chomp($password);
my $sqlupdate_user = "UPDATE PortalUser SET passwordHash=\'$passwordHash\' WHERE username=\'$username\';";
my $password_hash = readpipe "java -jar PasswordHasher.jar $password";
chomp($password_hash);
system "mysql -p $dbname -e \"$sqlupdate_user\"";

#TODO notify if something goes wrong with update
