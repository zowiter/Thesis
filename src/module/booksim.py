import subprocess
import csv

class Booksim:
    def __init__(self, topology, size, num_vcs, buf_size, routing_func):
        self.topology = topology
        self.size = size
        self.num_vcs = num_vcs
        self.buf_size = buf_size
        self.routing_func = routing_func

    
    def simulation(self, PATH):
        def frange(x, y, step):
            while x < y:
                yield x
                x += step

        def check_difference(a, b):
            diff_percent = abs(a - b) / max(a, b) * 100
            if diff_percent <= 20:
                return True
            else:
                return False
        
        traffics = ['uniform', 'bitcomp', 'bitrev', 'shuffle', 'transpose', 'tornado', 'neighbor']
        for traffic in traffics:
            print(f'Please, wait. Model {traffics.index(traffic)+1} is simulating')
            num_iter = 0
            flag = False
            for injection_rate in frange(0.25, 1.0, 0.1):
                num_iter += 1
                with open(PATH + "config\\config_booksim.txt", "w") as f:
                    f.write(f'''
                    // Topology
                    topology = {self.topology};
                    k = {self.size[0]};
                    n = {self.size[1]};

                    // Routing
                    routing_function = {self.routing_func};

                    // Flow control
                    num_vcs = {self.num_vcs};
                    vc_buf_size = {self.buf_size};

                    // Traffic
                    traffic = {traffic};
                    injection_rate = {round(injection_rate, 2)};
                    packet_size = 10;

                    max_samples = 5;
                    sim_type = throughput;
                    sample_period = 1000;
                    warmup_periods = 0;''')

                command = "booksim " + PATH + "config\\config_booksim.txt"
                new_directory = PATH + 'models\\BookSim\\src'

                result = subprocess.run(command, shell=True, capture_output=True, text=True, cwd=new_directory)
                with open(PATH + "result\\result_booksim.txt", "w") as f:
                    f.write(result.stdout)

                with open(PATH + "result\\result_booksim.txt", "r") as f:
                    text = f.read()
            
                lst = text.split("====== Traffic class 0 ======\n")
                text = lst[1]
                with open(PATH + "result\\result_booksim.txt", "w") as f:
                    f.write(text)
                
                with open(PATH + "result\\result_booksim.txt", "r") as f:
                    lines = list(f)

                flit_rate_avg = lines[21].replace('Accepted flit rate average = ', '')
                flit_rate_avg = flit_rate_avg.replace(' (1 samples)\n', '')
                flit_rate_avg = float(flit_rate_avg)

                int_size = self.size[0] * self.size[1]

                X = round(injection_rate, 2) * int_size
                Y = flit_rate_avg * int_size

                if (check_difference(X, Y) or num_iter == 1) and num_iter != 8:
                    beforeY = Y
                elif num_iter == 8 or check_difference(X,Y) == False:
                    network_throughput = beforeY
                    break   

            data = [['booksim', self.topology, traffic, int_size, network_throughput]]
            with open(PATH + 'result\\result.csv', 'a', newline='') as f:
                writer = csv.writer(f)
                for row in data:
                    writer.writerow(row)

        print('Simulation successful')