/*
 * Copyright (c) 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugin.quartz2;

import org.quartz.SimpleTrigger;
import org.quartz.Scheduler;

/**
 * <p>Holds plugin constants.</p>
 *
 * @author Micha?? K??ujszo
 * @author Graeme Rocher
 * @author Marcel Overdijk
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @see GrailsJobClass
 * @since 0.1
 */
public final class GrailsJobClassConstants {

    // restrict instantiation
    private GrailsJobClassConstants() {}

    public static final String EXECUTE = "execute";

    public static final String INTERRUPT = "interrupt";

    public static final String START_DELAY = "startDelay";

    public static final String CRON_EXPRESSION = "cronExpression";

    public static final String CONCURRENT = "concurrent";

    public static final String SESSION_REQUIRED = "sessionRequired";

    public static final String REPEAT_INTERVAL = "repeatInterval";

    public static final String REPEAT_COUNT = "repeatCount";

    public static final String DURABILITY = "durability";

    public static final String REQUESTS_RECOVERY = "requestsRecovery";

    public static final long DEFAULT_REPEAT_INTERVAL = 60000l;    // one minute

    public static final long DEFAULT_START_DELAY = 0l;  // no delay by default

    public static final String DEFAULT_CRON_EXPRESSION = "0 0 6 * * ?";

    public static final String DEFAULT_GROUP = Scheduler.DEFAULT_GROUP;

    public static final String DEFAULT_TRIGGERS_GROUP = Scheduler.DEFAULT_GROUP;

}
