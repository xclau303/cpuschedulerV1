import React, { useState, useEffect } from 'react'
import InputForm from './components/InputForm'
import OutputTable from './components/OutputTable'
import AnimatedSection from './components/AnimatedSection'
import SimulationHistory from './components/SimulationHistory'
import './index.css'

console.log("VITE_API_URL:", import.meta.env.VITE_API_URL);
const API_BASE = import.meta.env.VITE_API_URL;

export default function App() {
    const [currentPage, setCurrentPage] = useState("simulator")
    const [simulationHistory, setSimulationHistory] = useState([])
    const [results, setResults] = useState([])
    const [algorithm, setAlgorithm] = useState('fcfs')
    const [showWork, setShowWork] = useState(false)
    const [showAnimation, setShowAnimation] = useState(false)
    const [isLoading, setIsLoading] = useState(false)
    const [error, setError] = useState(null)
    const [ganttChart, setGanttChart] = useState([])
    const [averages, setAverages] = useState({ averageTAT: 0, averageWT: 0 })
    const [quantum, setQuantum] = useState(2)


    const [userSessionId, setUserSessionId] = useState(() => {
        let sessionId = localStorage.getItem('userSessionId');
        if (!sessionId) {
            sessionId = 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            localStorage.setItem('userSessionId', sessionId);
        }
        return sessionId;
    });

    const fetchSimulationHistory = async () => {
        try {
            const response = await fetch(`${API_BASE}/simulation-history?sessionId=${userSessionId}`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const historyData = await response.json();

            const transformedHistory = historyData.map(item => {
                const rawResults = JSON.parse(item.results);
                const rawGanttChart = JSON.parse(item.ganttChart);

                const transformedResults = rawResults.map(task => ({
                    id: String.fromCharCode(64 + task.id),
                    arrivalTime: task.arrivalTime,
                    burstTime: task.burstTime,
                    startTime: task.startTime,
                    finishTime: task.completionTime,
                    turnaroundTime: task.turnaroundTime,
                    waitingTime: task.waitingTime,
                    priority: task.priority || undefined,
                }));

                const transformedGanttChart = rawGanttChart.map(segment => ({
                    processId: segment.id === null ? "IDLE" : String.fromCharCode(64 + segment.id),
                    startTime: segment.startTime,
                    endTime: segment.endTime,
                    id: segment.id
                }));

                return {
                    id: item.id,
                    timestamp: item.timestamp,
                    algorithm: item.algorithm,
                    arrivalTimes: item.arrivalTimes,
                    burstTimes: item.burstTimes,
                    priorities: item.priorities,
                    quantum: item.quantum,
                    averages: {
                        averageTAT: item.averageTAT,
                        averageWT: item.averageWT
                    },
                    ganttChart: transformedGanttChart,
                    results: transformedResults,
                };
            });

            setSimulationHistory(transformedHistory);
        } catch (error) {
            console.error('Failed to fetch simulation history:', error);
        }
    };

    useEffect(() => {
        fetchSimulationHistory()
    }, [userSessionId])

    const handleSubmit = async (selectedAlgorithm, jobs, quantum) => {
        setError(null)
        setIsLoading(true)
        setAlgorithm(selectedAlgorithm)
        setQuantum(quantum)

        try {
            const result = await callBackendAPI(selectedAlgorithm, jobs, quantum)
            const transformedResults = result.scheduledTasks.map(task => {
                const pid = String.fromCharCode(64 + task.id)
                if (selectedAlgorithm === "rr") {
                    const processBlocks = result.ganttChart.filter(block => block.id === task.id)
                    const startTime = processBlocks.length > 0 ? processBlocks[0].startTime : task.startTime
                    const finishTime = processBlocks.length > 0 ? processBlocks[processBlocks.length - 1].endTime : task.completionTime
                    const turnaroundTime = finishTime - task.arrivalTime
                    const waitingTime = turnaroundTime - task.burstTime
                    return {
                        id: pid,
                        arrivalTime: task.arrivalTime,
                        burstTime: task.burstTime,
                        startTime,
                        finishTime,
                        turnaroundTime,
                        waitingTime,
                        priority: task.priority || undefined,
                    }
                } else {
                    return {
                        id: pid,
                        arrivalTime: task.arrivalTime,
                        burstTime: task.burstTime,
                        startTime: task.startTime,
                        finishTime: task.completionTime,
                        turnaroundTime: task.turnaroundTime,
                        waitingTime: task.waitingTime,
                        priority: task.priority || undefined,
                    }
                }
            })

            const transformedGanttChart = result.ganttChart.map(segment => ({
                processId: segment.id === null ? "IDLE" : String.fromCharCode(64 + segment.id),
                startTime: segment.startTime,
                endTime: segment.endTime,
                id: segment.id
            }))

            setResults(transformedResults)
            setGanttChart(transformedGanttChart)
            setAverages({
                averageTAT: result.averageTAT,
                averageWT: result.averageWT
            })

            await fetchSimulationHistory()
        } catch (e) {
            console.error("Backend API call failed:", e)
            setError("Failed to connect to backend: " + e.message)
        } finally {
            setIsLoading(false)
        }
    }

    const callBackendAPI = async (algorithm, jobs, quantum) => {
        const arrivalTimes = jobs.map(job => job.arrivalTime).join(' ')
        const burstTimes = jobs.map(job => job.burstTime).join(' ')
        const requestData = {
            arrivalTimes,
            burstTimes,
            sessionId: userSessionId
        }

        if (algorithm === 'priority') {
            requestData.priorities = jobs.map(job => job.priority).join(' ')
        }

        if (algorithm === 'rr') {
            requestData.quantum = quantum.toString()
        }

        const response = await fetch(`${API_BASE}/cpu-tasks/schedule/${algorithm}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData)
        })

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`)
        }

        return await response.json()
    }

    const loadFromHistory = (entry) => {
        setAlgorithm(entry.algorithm)
        setResults(entry.results)
        setGanttChart(entry.ganttChart)
        setAverages(entry.averages)
        setShowAnimation(true)
        setShowWork(true)
        setCurrentPage("simulator")
        setQuantum(entry.quantum)

    }

    const deleteFromHistory = async (id) => {
        try {
            const response = await fetch(`${API_BASE}/simulation-history/${id}?sessionId=${userSessionId}`, {
                method: 'DELETE'
            })
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            fetchSimulationHistory()
        } catch (error) {
            console.error('Failed to delete simulation:', error)
        }
    }

    return (
        <div className="container mx-auto p-6 bg-gray-50 min-h-screen">
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-4xl font-bold">CPU Scheduling Simulator</h1>
                <button
                    onClick={() => setCurrentPage(currentPage === "simulator" ? "history" : "simulator")}
                    className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700"
                >
                    {currentPage === "simulator" ? "History" : "Back to Simulator"}
                </button>
            </div>

            {currentPage === "simulator" ? (
                <>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="md:col-span-1">
                            <InputForm
                                onSubmit={handleSubmit}
                                showWork={showWork}
                                showAnimation={showAnimation}
                                onShowWorkChange={setShowWork}
                                onShowAnimationChange={setShowAnimation}
                                isLoading={isLoading}
                            />
                        </div>
                        <div className="md:col-span-2">
                            {results.length > 0 && (
                                <OutputTable
                                    algorithm={algorithm}
                                    results={results}
                                    showWork={showWork}
                                    ganttChart={ganttChart}
                                    averages={averages}
                                />
                            )}
                        </div>
                    </div>
                    {results.length > 0 && showAnimation && (
                        <div className="mt-8">
                            <AnimatedSection
                                results={results}
                                algorithm={algorithm}
                                quantum={quantum}
                                ganttChart={ganttChart}
                                hasRunSimulation={true}
                            />
                        </div>
                    )}
                </>
            ) : (
                <SimulationHistory
                    history={simulationHistory}
                    onLoadSimulation={loadFromHistory}
                    onDeleteSimulation={deleteFromHistory}
                />
            )}
            {error && <p className="text-red-600 text-center mt-4">{error}</p>}
        </div>
    )
}
