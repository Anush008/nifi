/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.aws.kinesis.stream;

import org.apache.nifi.processors.aws.testutil.AuthUtils;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.amazonaws.SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY;

// This integration test can be run against a mock Kenesis such as
// https://github.com/mhart/kinesalite or https://github.com/localstack/localstack
@Disabled("Required external service be running. Needs to be updated to make use of Localstack TestContainer")
public class ITPutKinesisStreamWithEndpointOverride {

    private TestRunner runner;

    @BeforeEach
    public void setUp() {
        System.setProperty(AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");

        runner = TestRunners.newTestRunner(PutKinesisStream.class);
        runner.setProperty(PutKinesisStream.KINESIS_STREAM_NAME, "test");
        runner.setProperty(PutKinesisStream.ENDPOINT_OVERRIDE, "http://localhost:4568");
        AuthUtils.enableAccessKey(runner, "accessKey", "secretKey");
        runner.assertValid();
    }

    @AfterEach
    public void tearDown() {
        runner = null;

        System.clearProperty(AWS_CBOR_DISABLE_SYSTEM_PROPERTY);
    }

    @Test
    public void testIntegrationSuccess() throws Exception {
        runner.assertValid();

        runner.enqueue("test".getBytes());
        runner.run(1);

        runner.assertAllFlowFilesTransferred(PutKinesisStream.REL_SUCCESS, 1);

        final List<MockFlowFile> ffs = runner.getFlowFilesForRelationship(PutKinesisStream.REL_SUCCESS);
        final MockFlowFile out = ffs.iterator().next();

        out.assertContentEquals("test".getBytes());
    }

    @Test
    public void testIntegrationFailedBadStreamName() throws Exception {
        runner.setProperty(PutKinesisStream.KINESIS_STREAM_NAME, "notfound");
        runner.assertValid();

        runner.enqueue("test".getBytes());
        runner.run(1);

        runner.assertAllFlowFilesTransferred(PutKinesisStream.REL_FAILURE, 1);

    }

}
