//
//  token.c
//  shenanigans_osx
//
//  Created by dr on 8/19/14.
//  Copyright (c) 2014 Shenanigans.io. All rights reserved.
//
#include "token.h"
#include <iostream>
#include <hashcash.h>


std::string* computeToken(std::string* input){

    int time_width = 6;
    unsigned int bits = 24;
    long anon_period = 0;
    char stampArray[200];
    char * stamp = stampArray;
   
    double triesTaken = 0;
    char *ext = NULL;
    int compress = 0;
    hashcash_callback callback = NULL;
    
    const char* resource = input->c_str();
    std::cout << "Computing token...";
    int result = hashcash_mint(time(0), time_width, resource, bits, anon_period, &stamp, NULL, &triesTaken, ext, compress, callback, NULL);
    std::cout << "Hashcash result " << result << std::endl;
    printf("Stamp: %s", stamp);
    if (result !=  HASHCASH_OK){
        return new std::string("");
    }
    std::cout << "Done." << std::endl;
    return new std::string(stamp);
}
