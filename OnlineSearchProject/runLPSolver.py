#import cplex
#import pulp
import os
import platform
import sys
import time
#from cplex.exceptions import CplexSolverError


def solveProblem(inputfile, outputfile,solverpath):
           	os.rename(inputfile,inputfile+".lp")
	#try:		
                inputfile=inputfile+".lp"
		start_time = time.time()
		outfile=outputfile+".txt"
		commandin="cbc "+ inputfile+ " solve solu " + outputfile
		if platform.system()== "Linux":
		    solverpath="LPSolver/"
		    os.chdir(solverpath)
		    commandin="./cbc "+ inputfile+ " solve solu " + outfile
		    os.system(commandin)
		    os.chdir("..")
		else:
		    solverpath="C:\\Users\\Marwan\\AppData\\Local\\Enthought\\Canopy\\User\\Lib\\site-packages\\pulp\\solverdir\\cbc\\win\\64\\"
		    commandin="cbc "+ inputfile+ " solve solu " + outputfile
		    os.system(solverpath+commandin)
		elapsed_time = time.time() - start_time
		print "Elapsed time: "+ str(elapsed_time)	

		#f=open("time.txt", 'a')
		#f.write(str(elapsed_time)+"\n")
		#f.close()
	        #except CplexSolverError:
		#print "Exception raised during solve"
		#return
		print outfile
                with open(outfile) as f:
                    content = f.readlines()
                f = open(outputfile, 'w')
                print outputfile
                print range(len(content))
                for i in range(1,len(content)):
                    if(content[i].split()[1].startswith('x') and int(content[i].split()[2])==1): 
                        varname = content[i].split()[1]
 	        	f.write(varname+"\t"+str(1.0)+"\n")
      	        f.write("***\n")
 	        f.close()

if __name__ == "__main__":
	solveProblem(sys.argv[1], sys.argv[2],sys.argv[3])
