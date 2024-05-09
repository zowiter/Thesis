import random

def is_integer(s):
    try:
        int(s)
        return True
    except ValueError:
        return False
    
class Helper:
    def __init__(self):
        pass

    def booksim_params(self):
        input_err = 1
        while input_err == 1:
            topology = str(input('Enter topology: "torus" or "mesh"\n').strip().lower())
            if  topology in ['torus', 'mesh']:
                input_err = 0
            else:
                print("Error: Write either 'mesh' or 'torus'")
                input_err = 1

        error = 1
        while error == 1:
            size = input('Enter size. For example: 4 4. The number of nodes should not exceed 16 and it should be a power of two.\n').split()
            n = int(size[0]) * int(size[1])
            if all(is_integer(element) for element in size) and n <= 16 and ((n & (n-1) == 0) and n != 0):
                size = [int(element) for element in size]
                error = 0
            else:
                print("Error: Entered data is not integers and the number of nodes should not exceed 16 and should be a number of two. Try again")

        error = 1
        while  error == 1:
            num_vcs = input('Enter munber of virual chanels. Default 4\n')
            if(is_integer(num_vcs)):
                num_vcs = int(num_vcs)
                error = 0
            else:
                print("Error: Entered data is not an integer. Please, enter only digits")

        error = 1
        while  error == 1:
            buf_size = input('Enter size of virual chanel buffer. Default 2\n')
            if(is_integer(buf_size)):
                buf_size = int(buf_size)
                error = 0
            else:
                print("Error: Entered data is not an integer. Please, enter only digits")

        if topology == 'mesh':
            routing_funcs = ['dim_order', 'dor']
        else:
            routing_funcs = ['dim_order']
        i = random.randint(0, len(routing_funcs)-1)
        routing_func = routing_funcs[i]

        return (topology, size, num_vcs, buf_size, routing_func)

    def newxim_params(self):
        input_err = 1
        while input_err == 1:
            topology = str(input('Enter topology: "MESH", "TORUS", "TREE" or "CIRCULANT"\n').strip().upper())
            if topology in ['MESH', 'TORUS', 'TREE', 'CIRCULANT']:
                input_err = 0
            else:
                print('Error: "MESH", "TORUS", "TREE" or "CIRCULANT"')
                input_err = 1

        error = 1
        while error == 1:
            size = input('Enter size. For example: 4 4\n').split()
            if all(is_integer(element) for element in size):
                # Преобразование элементов в список целых чисел
                size = [int(element) for element in size]
                error = 0
            else:
                print("Error: Entered data is not integers. Please, enter only digits.")

        error = 1
        while  error == 1:
            num_vsc = input('Enter munber of virual chanels. Default 4\n')
            if(is_integer(num_vsc)):
                num_vsc = int(num_vsc)
                error = 0
            else:
                print("Error: Entered data is not an integer. Please, enter only digits")
        
        return (topology, size, num_vsc)


    def gpnocsim_params(self):
        input_err = 1
        while input_err == 1:
            topology_choice = str(input('Enter topology: "torus" or "mesh"\n').strip().lower())
            if  topology_choice in ['torus', 'mesh']:
                input_err = 0
                if topology_choice == 'mesh':
                    topology = 1
                else:
                    topology = 2
            else:
                print("Error: Write either 'mesh' or 'torus'")
                input_err = 1
        
        error = 1
        while error == 1:
            size = input('Enter size. For example: 16\n')
            if is_integer(size):
                size = int(size)
                error = 0
            else:
                print("Error: Entered data is not integers. Please, enter only digits.")

        error = 1
        while  error == 1:
            num_vsc = input('Enter munber of virual chanels. Default 4\n')
            if is_integer(num_vsc):
                num_vsc = int(num_vsc)
                error = 0
            else:
                print("Error: Entered data is not an integer. Please, enter only digits")

        return (topology, size, num_vsc)

    def topaz_params(self):
        input_err = 1
        while input_err == 1:
            topology = str(input('Enter topology: "torus" or "mesh"\n').strip().lower())
            if  topology in ['torus', 'mesh']:
                input_err = 0
            else:
                print("Error: Write either 'mesh' or 'torus'")
                input_err = 1
        error = 1
        while error == 1:
            size = input('Enter size. For example: 4 4\n').split()
            if all(is_integer(element) for element in size):
                # Преобразование элементов в список целых чисел
                size = [int(element) for element in size]
                error = 0
            else:
                print("Error: Entered data is not integers. Please, enter only digits.")
        
        return (topology, size)
            
        