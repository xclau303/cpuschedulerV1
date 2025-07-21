// Utility functions to transform backend data to frontend format
export const transformBackendResponse = (backendData, algorithm) => {
    // Transform scheduled tasks to match frontend format
    const transformedResults = backendData.scheduledTasks.map((task) => ({
        id: String.fromCharCode(64 + task.id), // Convert 1,2,3... to A,B,C...
        arrivalTime: task.arrivalTime,
        burstTime: task.burstTime,
        finishTime: task.completionTime,
        turnaroundTime: task.turnaroundTime,
        waitingTime: task.waitingTime,
        priority: task.priority || 1,
        remainingTime: task.remainingTime || 0,
    }))

    // Transform Gantt chart data
    let ganttChart = []

    if (backendData.ganttChart && backendData.ganttChart.length > 0) {
        // For Round Robin, use the ganttChart from backend
        ganttChart = backendData.ganttChart.map((block) => ({
            processId: String.fromCharCode(64 + block.id),
            startTime: block.startTime,
            endTime: block.endTime,
        }))
    } else {
        // For other algorithms, construct gantt chart from scheduled tasks
        ganttChart = constructGanttChart(transformedResults, backendData.idleTimes)
    }

    // Add idle times to gantt chart if they exist
    if (backendData.idleTimes && backendData.idleTimes.length > 0) {
        backendData.idleTimes.forEach((idle) => {
            ganttChart.push({
                processId: "IDLE",
                startTime: idle.startTime,
                endTime: idle.endTime,
            })
        })

        // Sort gantt chart by start time
        ganttChart.sort((a, b) => a.startTime - b.startTime)
    }

    return {
        results: transformedResults,
        ganttChart: ganttChart,
        averages: {
            turnaround: backendData.averageTAT,
            waiting: backendData.averageWT,
        },
    }
}

const constructGanttChart = (results, idleTimes) => {
    // For FCFS, SJF, and Priority - construct gantt chart from completion times
    const gantt = results.map((task) => ({
        processId: task.id,
        startTime: task.finishTime - task.burstTime,
        endTime: task.finishTime,
    }))

    // Sort by start time
    gantt.sort((a, b) => a.startTime - b.startTime)

    return gantt
}
