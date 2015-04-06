/*
 *  @author Philip Stutz
 *  
 *  Copyright 2012 University of Zurich
 *      
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *         http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.signalcollect.nodeprovisioning.local

import akka.actor.ActorRef
import com.signalcollect.configuration.GraphConfiguration
import com.signalcollect.nodeprovisioning.NodeProvisioner
import com.signalcollect.nodeprovisioning.Node
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.signalcollect.configuration.AkkaConfig

class LocalNodeProvisioner extends NodeProvisioner {

    def getNodes: List[Node] = {
        List(new LocalNode())
    }
}