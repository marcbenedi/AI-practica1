#imports
import subprocess
import csv
from matplotlib import pyplot as plt
import numpy as np

#Configuration of the experiment
seed_c = 1234
seed_s = 4321

num_c = 4
num_s = 100

operant_id = 1
initial_solution_id = 1

program = []

program.append("java")
program.append("-jar")
program.append("CodiPractica/out/artifacts/CodiPractica_jar/CodiPractica.jar")

program.append(str(num_c))
program.append(str(num_s))
program.append(str(seed_c))
program.append(str(seed_s))
program.append(str(operant_id))
program.append(str(initial_solution_id))



#Init of script
#print("Launching experiment 1 with the next configuration:")
#print("seed_c =", seed_c)
#print("seed_s =", seed_s)
#print("num_c =", num_c)
#print("num_s =", num_s)
#print("operant_id =", operant_id)
#print("initial_solution_id =", initial_solution_id)

#numc nums seedc seeds opid genid

seeds_s = [1313, 1122, 2233, 3344, 4455, 5566, 6677, 7788, 8899, 9900]
seeds_c = [1100, 2211, 3322, 4433, 5544, 6655, 7766, 8877, 9988, 3141]

listaHeurist = [[],[]]
listaCoste = [[],[]]
listaDatos = [[],[]]

listaMilis = [[],[]]
listaExpanded= [[],[]]


for rep in range(10):
    program[5] = str(seeds_c[rep])
    program[6] = str(seeds_s[rep])
    for op in range(1,3):
        program[-1] = str(op)
        output = subprocess.Popen(program, stdout=subprocess.PIPE).communicate()[0]
        #file = open("./experiment2/"+ str(rep) + "-"+ str(op) +".out","w")
        #file.write(output)
        #file.close()
        #print output
        reader = csv.reader(output.splitlines())
        listaCsv = list(reader)
        listaHeurist[op-1].append(eval(listaCsv[-3][2]))
        listaCoste[op-1].append(eval(listaCsv[-3][1]))
        listaDatos[op-1].append(eval(listaCsv[-3][0]))
        listaMilis[op-1].append(eval(listaCsv[-2][0]))
        listaExpanded[op-1].append(eval(listaCsv[-1][0]))

plt.boxplot(listaHeurist)
plt.suptitle('Heuristic')
plt.autoscale()
plt.xticks([1,2],['Estrategia greedy','Connectats en linia'])
plt.xlabel('Estrategia solucio inicial')
plt.ylabel('Valor heuristic')
plt.savefig('./experiment2/heuristic.png',bbox_inches='tight')
plt.clf()
plt.boxplot(listaCoste)
plt.suptitle('Cost')
plt.xticks([1,2],['Estrategia greedy','Connectats en linia'])
plt.xlabel('Estrategia solucio inicial')
plt.ylabel('Cost de la xarxa')
plt.savefig('./experiment2/coste.png',bbox_inches='tight')
plt.clf()
plt.boxplot(listaDatos)
plt.suptitle('Dades rebudes (respecte el total que es recull)')
plt.xticks([1,2],['Estrategia greedy','Connectats en linia'])
plt.xlabel('Estrategia solucio inicial')
plt.ylabel('Dades rebudes')
plt.savefig('./experiment2/datos.png',bbox_inches='tight')
plt.clf()
plt.boxplot(listaMilis)
plt.suptitle('Temps')
plt.xticks([1,2],['Estrategia greedy','Connectats en linia'])
plt.xlabel('Estrategia solucio inicial')
plt.ylabel('ms')
plt.savefig('./experiment2/tiempo.png',bbox_inches='tight')
plt.clf()
