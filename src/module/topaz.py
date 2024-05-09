import subprocess
import csv

class Topaz:

    def __init__(self, topology, size):
        self.topology = topology
        self.size  = size

    def simulation(self, PATH):
        with open(PATH + 'models\\tpzsimul\\sgm\\Network.sgm', 'r', encoding='utf-8') as file:
            lines = file.readlines()
        if self.topology == 'mesh':
            search_prefix = '<MeshNetwork id="M44_CT_NOC"'
            replacement_line = f'<MeshNetwork id="M44_CT_NOC" sizeX={self.size[0]}  sizeY={self.size[1]}  router="MESH-CT-NOC" delay=0>\n'
            with open(PATH + 'models\\tpzsimul\\sgm\\Network.sgm', 'w', encoding='utf-8') as file:
                for line in lines:
                    if line.startswith(search_prefix):
                        file.write(replacement_line)
                    else:
                        file.write(line)
        else:
            search_prefix = '<TorusNetwork id="T44_CT_NOC"'
            replacement_line = f'<TorusNetwork id="T44_CT_NOC" sizeX={self.size[0]}  sizeY={self.size[1]}  router="TORUS-CT-NOC" delay=0>\n'
            with open(PATH + 'models\\tpzsimul\\sgm\\Network.sgm', 'w', encoding='utf-8') as file:
                for line in lines:
                    if line.startswith(search_prefix):
                        file.write(replacement_line)
                    else:
                        file.write(line)

        traffics = ['RANDOM', 'BIT-REVERSAL', 'PERFECT-SUFFLE', 'PERMUTATION', 'TORNADO']
        for traffic in traffics:
            if self.topology == 'mesh':
                command = f"TPZSimul.exe -s M44-CT-NOC -c 1000 -l 0.1 -L 10 -t {traffic} -D"
            else:
                command = f"TPZSimul.exe -s T44-CT-NOC -c 1000 -l 0.1 -L 10 -t {traffic} -D"
            
            new_directory = PATH + 'models\\tpzsimul\\mak'

            print(f'Please, wait. Model {traffics.index(traffic)+1} is simulating')

            result = subprocess.run(command, shell=True, capture_output=True, text=True, cwd=new_directory)
            with open(PATH + "result\\result_topaz.txt", "w") as f:
                f.write(result.stdout)

            with open(PATH + "result\\result_topaz.txt", "r") as f:
                text = f.read()
                f.close()
            lst = text.split("************************ PERFORMANCE ***********************\n")
            text = lst[1]
            with open(PATH + "result\\result_topaz.txt", "w") as f:
                f.write(text)

            with open(PATH + "result\\result_topaz.txt", "r") as f:
                lines = list(f)

            line = str(lines[3])
            line = line.replace(" Throughput              = ", "")
            line = line.split(' f/c ')[0]
            result = line + '000'

            int_size = self.size[0] * self.size[1]
            data = [['topaz', self.topology.lower(), traffic.lower(), int_size, result]]
            with open(PATH + 'result\\result.csv', 'a', newline='') as f:
                writer = csv.writer(f)
                for row in data:
                    writer.writerow(row)

        print('Simulation successful')