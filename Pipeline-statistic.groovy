import com.cloudbees.workflow.rest.external.*

def JOB_INIT_STAGE = 'Job initialization'
def JOB_NAME 	   = 'Your-Pipeline-Job-Name' // Job name
def BUILD_NUMBERS  = 1000                     // Number of last builds for calculate statistic
  

def job    = Jenkins.instance.getItemByFullName(JOB_NAME)
def builds = job.builds.limit(BUILD_NUMBERS)
Map<String, List<Long>> stagesMap = new LinkedHashMap<>()


builds.each { build ->
    def runExt = RunExt.create(build)
    if(runExt.stages){
        long queueDurationMillis = runExt.stages.get(0).startTimeMillis - build.getStartTimeInMillis()
        stagesMap.put(JOB_INIT_STAGE,
                stagesMap.getOrDefault(JOB_INIT_STAGE, new ArrayList<Long>()) << Math.max(0, queueDurationMillis))
    }
    runExt.stages.each { stage ->
        stagesMap.put(stage.name, stagesMap.getOrDefault(stage.name, new ArrayList<Long>()) << stage.durationMillis)
    }
}

stagesMap.each { key, val ->
    long count = 0
    long sum = 0
    long min = Long.MAX_VALUE
    long max = Long.MIN_VALUE
    val.each { value ->
        ++count
        sum += value
        min = Math.min(min, value)
        max = Math.max(max, value)
    }
    println "${key}: {\n\tcount=${count},\n\tsum=${sum},\n\tmin=${min},\n\tmax=${max},\n\taverage=${count > 0 ? (double) sum / count : 0.0d}\n}"
}
