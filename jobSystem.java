import java.util.Scanner;

public class jobSystem {
    private job[] jobArray; // Array to store jobs
    private int jobCount; // Number of jobs in the system
    private job[] readyJobs; // Array to keep track of jobs that are ready to be executed
    private int readyJobCount; // Count of ready jobs
    private job[] queue; // Queue to manage jobs that are ready for execution
    private int Size; // Size of the queue
    private int Front; // Front of the queue
    private int Rear; // Rear of the queue

    public jobSystem() {
        jobArray = new job[10]; // Initialize the job array with size 10
        jobCount = 0; // Initialize job count to 0
        readyJobs = new job[10]; // Initialize the ready jobs array with size 10
        readyJobCount = 0; // Initialize ready job count to 0
        queue = new job[10]; // Initialize the queue with size 10
        Size = 0; // Initialize queue size to 0
        Front = 0; // Initialize queue front to 0
        Rear = 0; // Initialize queue rear to 0
    }

    public void addingJobToSystem(job job) {
        // Add job to the job array if it doesn't already exist
        if (findJobIndexById(job.getID()) != -1) {
            System.out.println("Job with job ID " + job.getID() + " is already there.");
            return;
        }
        if (jobCount == jobArray.length) {
            expandJobArray();
        }
        jobArray[jobCount++] = job;
    }

    private int findJobIndexById(int jobID) {
        // Find job index by ID using linear search
        for (int i = 0; i < jobCount; i++) {
            if (jobArray[i].getID() == jobID) {
                return i;
            }
        }
        return -1; // Job not found
    }

    public boolean isThisJobReady(job job) {
        // Check if all dependencies of the job are completed
        for (int dependencyID : job.getDependencies()) {
            int index = findJobIndexById(dependencyID);
            if (index == -1 || !jobArray[index].getStatus().equals("Completed")) {
                return false;
            }
        }
        return true;
    }

    public void updateReadyJobs() {
        // Update the list of jobs that are ready to be executed
        readyJobCount = 0;
        for (int i = 0; i < jobCount; i++) {
            job job = jobArray[i];
            if (isThisJobReady(job) && !job.getStatus().equals("Completed")) {
                if (readyJobCount == readyJobs.length) {
                    expandReadyJobs();
                }
                readyJobs[readyJobCount++] = job;
            }
        }
    }

    private void expandJobArray() {
        // Double the size of the job array if it is full
        job[] newJobArray = new job[jobArray.length * 2];
        System.arraycopy(jobArray, 0, newJobArray, 0, jobArray.length);
        jobArray = newJobArray;
    }

    private void expandReadyJobs() {
        // Double the size of the readyJobs array if it is full
        job[] newReadyJobs = new job[readyJobs.length * 2];
        System.arraycopy(readyJobs, 0, newReadyJobs, 0, readyJobs.length);
        readyJobs = newReadyJobs;
    }

    public void enqueue(job job) {
        // Add a job to the queue
        if (Size == queue.length) {
            expandQueue();
        }
        queue[Rear] = job;
        Rear = (Rear + 1) % queue.length;
        Size++;
    }

    public job dequeue() {
        // Remove a job from the queue
        if (Size == 0) {
            throw new IllegalStateException("Queue is Empty");
        }
        job job = queue[Front];
        queue[Front] = null;
        Front = (Front + 1) % queue.length;
        Size--;
        return job;
    }

    private void expandQueue() {
        // Double the size of the queue if it is full
        job[] newQueue = new job[queue.length * 2];
        for (int i = 0; i < Size; i++) {
            newQueue[i] = queue[(Front + i) % queue.length];
        }
        queue = newQueue;
        Front = 0;
        Rear = Size;
    }

    public void executeJobs(job job) {
        // Execute a job and mark it as completed
        job.setStatus("Completed");
        System.out.println("Job " + job.getID() + " executed and marked as Completed");
    }


    public static void main(String[] args) {
        jobSystem jobSystem = new jobSystem(); // Create a new job system
        Scanner input = new Scanner(System.in);

        System.out.println("Enter number of jobs that you want to enter to your System: ");
        int numberOfJobs = input.nextInt(); // Get the number of jobs to be entered
        input.nextLine();

        for (int i = 0; i < numberOfJobs; i++) {
            // Enter details for each job
            System.out.println("Job " + (i + 1) + ": ");
            int jobID;
            while (true) {
                System.out.print("Enter your job ID: ");
                jobID = input.nextInt();
                input.nextLine();
                if (jobSystem.findJobIndexById(jobID) == -1) {
                    break;
                } else {
                    System.out.println("Job ID " + jobID + " is already there. Please enter another job ID.");
                }
            }

            job job = new job(jobID);

            String status;
            while (true) {
                // Enter the status of the job
                System.out.print("Enter job status (Completed / Not Completed): ");
                status = input.nextLine();
                if (status.equals("Completed") || status.equals("Not Completed")) {
                    break;
                } else {
                    System.out.println("Invalid. Please enter again.");
                }
            }
            job.setStatus(status);

            System.out.print("Enter number of dependencies: ");
            int numberOfDependencies = input.nextInt();
            input.nextLine();

            for (int j = 0; j < numberOfDependencies; j++) {
                // Enter dependencies for the job
                System.out.print("Enter dependency " + (j + 1) + " ID: ");
                int dependencyID = input.nextInt();
                input.nextLine();

                if (!job.addDependency(dependencyID)) {
                    System.out.println("Dependency ID " + dependencyID + " is already there.");
                }
            }
            jobSystem.addingJobToSystem(job);
            System.out.println("Job has been added to the system successfully!");
            System.out.println(" ");
        }
        jobSystem.updateReadyJobs(); // Update the list of ready jobs

        while (jobSystem.readyJobCount > 0) {
            // Enqueue and execute ready jobs
            for (int i = 0; i < jobSystem.readyJobCount; i++) {
                job job = jobSystem.readyJobs[i];
                if (areDependenciesCompleted(job, jobSystem)) {
                    jobSystem.enqueue(job);
                }
            }
            while (jobSystem.Size > 0) {
                job job = jobSystem.dequeue();
                System.out.println("Next job to be executed: " + job.getID());
                jobSystem.executeJobs(job);
            }
            jobSystem.updateReadyJobs();
        }

        // Check for jobs that could not be executed
        boolean allJobsExecuted = true;
        for (int i = 0; i < jobSystem.jobCount; i++) {
            job job = jobSystem.jobArray[i];
            if (!areDependenciesCompleted(job, jobSystem)) {
                allJobsExecuted = false;
                System.out.println("Job " + job.getID() + " could not be executed due to incomplete dependencies.");
            }
        }

        if (allJobsExecuted) {
            System.out.println("Therefore, All jobs executed.");
        } else {
            System.out.println("Some jobs could not be executed due to unmet dependencies.");
        }
    }

    private static boolean areDependenciesCompleted(job job, jobSystem jobSystem) {
        // Check if all dependencies of a job are completed
        for (int dependencyID : job.getDependencies()) {
            int index = jobSystem.findJobIndexById(dependencyID);
            if (index == -1) {
                System.out.println("Dependency ID " + dependencyID + " for job " + job.getID() + " is not present in the system.");
                return false;
            }
            if (!jobSystem.jobArray[index].getStatus().equals("Completed")) {
                return false;
            }
        }
        return true;
    }
}

// job class definition remains the same

class job {
    private int ID; // Job ID
    private String status; // Job status (Completed / Not Completed)
    private int[] dependencies; // Array of dependency IDs
    private int dependenciesCount; // Count of dependencies

    public job(int ID) {
        this.ID = ID;
        this.status = "Not Completed"; // Initialize job status as Not Completed
        this.dependencies = new int[10]; // Initialize dependencies array with size 10
        this.dependenciesCount = 0; // Initialize dependencies count to 0
    }

    public int getID() {
        return ID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int[] getDependencies() {
        int[] result = new int[dependenciesCount];
        System.arraycopy(dependencies, 0, result, 0, dependenciesCount);
        return result;
    }

    private boolean containsDependency(int dependencyID) {
        // Check if a dependency is already present
        for (int i = 0; i < dependenciesCount; i++) {
            if (dependencies[i] == dependencyID) {
                return true;
            }
        }
        return false;
    }

    private void expandDependency() {
        // Double the size of the dependencies array if it is full
        int[] newDependencies = new int[dependencies.length * 2];
        System.arraycopy(dependencies, 0, newDependencies, 0, dependencies.length);
        dependencies = newDependencies;
    }

    public boolean addDependency(int dependencyID) {
        // Add a dependency to the job
        if (containsDependency(dependencyID)) {
            return false;
        }
        if (dependenciesCount == dependencies.length) {
            expandDependency();
        }
        dependencies[dependenciesCount++] = dependencyID;
        return true;
    }
}