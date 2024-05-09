import subprocess
import csv

class Gpnocsim:
    def  __init__(self, topology, size, num_vcs):
        self.topology = topology
        self.size = size
        self.num_vcs = num_vcs

    def simulation(self, PATH):
        traffics = [0, 1]
        for traffic in traffics:
            with open(PATH + "nocSimOutput.txt", 'w+') as f:
                pass
            with open(PATH + "models\\io\\nocSimParameter.txt", 'w+') as f:
                pass
            with open(PATH + "models\\io\\nocSimParameter.txt", 'a') as f:
                f.write(f'\nCURRENT_NET = {self.topology}\nWK_W = 4\nWK_L = 4\nAVG_INTER_ARRIVAL = 150\nAVG_MESSAGE_LENGTH = 10\nFLIT_LENGTH = 55\nNUMBER_OF_IP_NODE = {self.size}\nCURRENT_VC_COUNT = {self.num_vcs}\n' +
                f'NUM_FLIT_PER_BUFFER = 1\nNUM_CYCLE = 1000\nNUM_RUN = 30\nTRAFFIC_TYPE = {traffic}\nWARM_UP_CYCLE = 0.1\nFIXED_MESSAGE_LENGTH = false\nTRACE = false\nASYNCHRONOUS = true\nDEBUG = false\n')
            
            print(f'Please, wait. Model {traffics.index(traffic)+1} is simulating')
            
            command = "java -jar gpCmd-1.0.jar"
            new_directory = PATH + "models\\"
            result = subprocess.run(command, shell=True, capture_output=True, text=True, cwd=new_directory)

            with open(PATH + "models\\io\\nocSimOutput.txt", "r") as f:
                text = f.read()
                f.close()
            lst = text.split(" .....................\nPerformance Measurements.......\n")
            text = lst[1]
            with open(PATH + "models\\io\\nocSimOutput.txt", "w") as f:
                f.write(text)
                f.close()
            with open(PATH + "models\\io\\nocSimOutput.txt", "r") as f:
                lines = list(f)
                f.close()
            line = str(lines[1])
            line = line.replace("  ", "")
            line = line.replace("Throughput[Flits leaving network.unit.switches.Switch]	", "")
            line = line.replace("\n", "")
            result = float(line)
            if traffic == 0:
                pattern = 'uniform'
            else:
                pattern = 'local'
            
            if self.topology == 1:
                topology_choice = 'mesh'
            else:
                topology_choice = 'torus'
                
            data = [['gpnocsim', topology_choice, pattern, int(self.size), result]]
            with open(PATH + 'result\\result.csv', 'a', newline='') as f:
                writer = csv.writer(f)
                for row in data:
                    writer.writerow(row)

        print('Simulation successful')