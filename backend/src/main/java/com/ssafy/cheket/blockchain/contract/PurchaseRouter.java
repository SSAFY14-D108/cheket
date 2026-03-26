package com.ssafy.cheket.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>
 * Auto generated code.
 * <p>
 * <strong>Do not modify!</strong>
 * <p>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j
 * command line tools</a>, or the
 * org.web3j.codegen.SolidityFunctionWrapperGenerator in the <a href=
 * "https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen
 * module</a> to update.
 *
 * <p>
 * Generated with web3j version 4.12.3.
 */
@SuppressWarnings("rawtypes")
public class PurchaseRouter extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b50604051612117380380612117833981810160405281019061003291906102e4565b61004e6100436101b560201b60201c565b6101bd60201b60201c565b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16036100bd576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016100b490610381565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff160361012c576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610123906103ed565b60405180910390fd5b81600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600560006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505061040d565b600033905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600080fd5b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006102b182610286565b9050919050565b6102c1816102a6565b81146102cc57600080fd5b50565b6000815190506102de816102b8565b92915050565b600080604083850312156102fb576102fa610281565b5b6000610309858286016102cf565b925050602061031a858286016102cf565b9150509250929050565b600082825260208201905092915050565b7f496e76616c696420535346000000000000000000000000000000000000000000600082015250565b600061036b600b83610324565b915061037682610335565b602082019050919050565b6000602082019050818103600083015261039a8161035e565b9050919050565b7f496e76616c696420706c6174666f726d2077616c6c6574000000000000000000600082015250565b60006103d7601783610324565b91506103e2826103a1565b602082019050919050565b60006020820190508181036000830152610406816103ca565b9050919050565b611cfb8061041c6000396000f3fe608060405234801561001057600080fd5b506004361061009e5760003560e01c8063c77836f911610066578063c77836f914610121578063f2fde38b1461013f578063f738c4a71461015b578063fa2af9da14610179578063fb74dabc146101975761009e565b80632f5eb679146100a3578063715018a6146100c15780638da5cb5b146100cb5780639c6868fc146100e9578063b3066d4914610105575b600080fd5b6100ab6101b5565b6040516100b8919061102d565b60405180910390f35b6100c96101db565b005b6100d36101ef565b6040516100e0919061102d565b60405180910390f35b61010360048036038101906100fe91906110be565b610218565b005b61011f600480360381019061011a9190611111565b610b6a565b005b610129610d87565b604051610136919061102d565b60405180910390f35b61015960048036038101906101549190611164565b610dad565b005b610163610e30565b604051610170919061102d565b60405180910390f35b610181610e56565b60405161018e919061102d565b60405180910390f35b61019f610e7c565b6040516101ac91906111f0565b60405180910390f35b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6101e3610ea2565b6101ed6000610f20565b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b610220610ea2565b600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff160361028f576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161028690611268565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1603610320576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610317906112d4565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16036103b1576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016103a890611340565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1603610442576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610439906113ac565b60405180910390fd5b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690506000600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690506000600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905060008373ffffffffffffffffffffffffffffffffffffffff1663e7572230876040518263ffffffff1660e01b81526004016104f291906113db565b602060405180830381865afa15801561050f573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190610533919061140b565b905060008111610578576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161056f90611484565b60405180910390fd5b60008473ffffffffffffffffffffffffffffffffffffffff166350b44712886040518263ffffffff1660e01b81526004016105b391906113db565b600060405180830381865afa1580156105d0573d6000803e3d6000fd5b505050506040513d6000823e3d601f19601f820116820180604052508101906105f9919061163e565b505050505050505090508273ffffffffffffffffffffffffffffffffffffffff1663591ed976826040518263ffffffff1660e01b815260040161063c91906113db565b602060405180830381865afa158015610659573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061067d9190611778565b6106bc576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016106b3906117f1565b60405180910390fd5b60008373ffffffffffffffffffffffffffffffffffffffff16631d645b12836040518263ffffffff1660e01b81526004016106f791906113db565b600060405180830381865afa158015610714573d6000803e3d6000fd5b505050506040513d6000823e3d601f19601f8201168201806040525081019061073d9190611811565b505050509250505060008673ffffffffffffffffffffffffffffffffffffffff1663fac38f8e848a8d6040518463ffffffff1660e01b8152600401610784939291906118cf565b602060405180830381865afa1580156107a1573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906107c5919061140b565b9050818110610809576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161080090611952565b60405180910390fd5b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd8c600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16886040518463ffffffff1660e01b815260040161088c93929190611972565b6020604051808303816000875af11580156108ab573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906108cf9190611778565b905080610911576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610908906119f5565b60405180910390fd5b8673ffffffffffffffffffffffffffffffffffffffff1663627c82f78a876040518363ffffffff1660e01b815260040161094c929190611a15565b600060405180830381600087803b15801561096657600080fd5b505af115801561097a573d6000803e3d6000fd5b505050508773ffffffffffffffffffffffffffffffffffffffff16638cd9e9c28b6040518263ffffffff1660e01b81526004016109b791906113db565b600060405180830381600087803b1580156109d157600080fd5b505af11580156109e5573d6000803e3d6000fd5b505050508773ffffffffffffffffffffffffffffffffffffffff166323b872dd600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff168d8d6040518463ffffffff1660e01b8152600401610a4893929190611972565b600060405180830381600087803b158015610a6257600080fd5b505af1158015610a76573d6000803e3d6000fd5b505050508773ffffffffffffffffffffffffffffffffffffffff16639b7377fa858b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff168f6040518563ffffffff1660e01b8152600401610adb9493929190611a3e565b600060405180830381600087803b158015610af557600080fd5b505af1158015610b09573d6000803e3d6000fd5b50505050888b73ffffffffffffffffffffffffffffffffffffffff168b7fef266bb11bf4b58aa8562ab8c8746e3b84a521780a2c57ca09d87bae13f5eb0988604051610b5591906113db565b60405180910390a45050505050505050505050565b610b72610ea2565b600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610be1576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610bd890611acf565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610c50576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610c4790611b3b565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1603610cbf576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610cb690611ba7565b60405180910390fd5b82600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555081600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600460006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505050565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b610db5610ea2565b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1603610e24576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610e1b90611c39565b60405180910390fd5b610e2d81610f20565b50565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b610eaa610fe4565b73ffffffffffffffffffffffffffffffffffffffff16610ec86101ef565b73ffffffffffffffffffffffffffffffffffffffff1614610f1e576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610f1590611ca5565b60405180910390fd5b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600033905090565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b600061101782610fec565b9050919050565b6110278161100c565b82525050565b6000602082019050611042600083018461101e565b92915050565b6000604051905090565b600080fd5b600080fd5b6110658161100c565b811461107057600080fd5b50565b6000813590506110828161105c565b92915050565b6000819050919050565b61109b81611088565b81146110a657600080fd5b50565b6000813590506110b881611092565b92915050565b6000806000606084860312156110d7576110d6611052565b5b60006110e586828701611073565b93505060206110f6868287016110a9565b9250506040611107868287016110a9565b9150509250925092565b60008060006060848603121561112a57611129611052565b5b600061113886828701611073565b935050602061114986828701611073565b925050604061115a86828701611073565b9150509250925092565b60006020828403121561117a57611179611052565b5b600061118884828501611073565b91505092915050565b6000819050919050565b60006111b66111b16111ac84610fec565b611191565b610fec565b9050919050565b60006111c88261119b565b9050919050565b60006111da826111bd565b9050919050565b6111ea816111cf565b82525050565b600060208201905061120560008301846111e1565b92915050565b600082825260208201905092915050565b7f496e76616c696420627579657200000000000000000000000000000000000000600082015250565b6000611252600d8361120b565b915061125d8261121c565b602082019050919050565b6000602082019050818103600083015261128181611245565b9050919050565b7f5469636b65744e4654206e6f7420736574000000000000000000000000000000600082015250565b60006112be60118361120b565b91506112c982611288565b602082019050919050565b600060208201905081810360008301526112ed816112b1565b9050919050565b7f536574746c656d656e74206e6f74207365740000000000000000000000000000600082015250565b600061132a60128361120b565b9150611335826112f4565b602082019050919050565b600060208201905081810360008301526113598161131d565b9050919050565b7f4576656e744e4654206e6f742073657400000000000000000000000000000000600082015250565b600061139660108361120b565b91506113a182611360565b602082019050919050565b600060208201905081810360008301526113c581611389565b9050919050565b6113d581611088565b82525050565b60006020820190506113f060008301846113cc565b92915050565b60008151905061140581611092565b92915050565b60006020828403121561142157611420611052565b5b600061142f848285016113f6565b91505092915050565b7f496e76616c6964207469636b6574207072696365000000000000000000000000600082015250565b600061146e60148361120b565b915061147982611438565b602082019050919050565b6000602082019050818103600083015261149d81611461565b9050919050565b600080fd5b600080fd5b6000601f19601f8301169050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b6114f7826114ae565b810181811067ffffffffffffffff82111715611516576115156114bf565b5b80604052505050565b6000611529611048565b905061153582826114ee565b919050565b600067ffffffffffffffff821115611555576115546114bf565b5b61155e826114ae565b9050602081019050919050565b60005b8381101561158957808201518184015260208101905061156e565b60008484015250505050565b60006115a86115a38461153a565b61151f565b9050828152602081018484840111156115c4576115c36114a9565b5b6115cf84828561156b565b509392505050565b600082601f8301126115ec576115eb6114a4565b5b81516115fc848260208601611595565b91505092915050565b600060ff82169050919050565b61161b81611605565b811461162657600080fd5b50565b60008151905061163881611612565b92915050565b60008060008060008060008060006101208a8c03121561166157611660611052565b5b600061166f8c828d016113f6565b99505060206116808c828d016113f6565b98505060408a015167ffffffffffffffff8111156116a1576116a0611057565b5b6116ad8c828d016115d7565b97505060606116be8c828d016113f6565b96505060806116cf8c828d016113f6565b95505060a08a015167ffffffffffffffff8111156116f0576116ef611057565b5b6116fc8c828d016115d7565b94505060c061170d8c828d016113f6565b93505060e061171e8c828d01611629565b9250506101006117308c828d016113f6565b9150509295985092959850929598565b60008115159050919050565b61175581611740565b811461176057600080fd5b50565b6000815190506117728161174c565b92915050565b60006020828403121561178e5761178d611052565b5b600061179c84828501611763565b91505092915050565b7f426f6f6b696e67206e6f74206f70656e00000000000000000000000000000000600082015250565b60006117db60108361120b565b91506117e6826117a5565b602082019050919050565b6000602082019050818103600083015261180a816117ce565b9050919050565b600080600080600080600060e0888a0312156118305761182f611052565b5b600088015167ffffffffffffffff81111561184e5761184d611057565b5b61185a8a828b016115d7565b975050602061186b8a828b016113f6565b965050604061187c8a828b016113f6565b955050606061188d8a828b016113f6565b945050608061189e8a828b016113f6565b93505060a06118af8a828b016113f6565b92505060c06118c08a828b01611763565b91505092959891949750929550565b60006060820190506118e460008301866113cc565b6118f160208301856113cc565b6118fe604083018461101e565b949350505050565b7f45786365656473206d6178207065722077616c6c657400000000000000000000600082015250565b600061193c60168361120b565b915061194782611906565b602082019050919050565b6000602082019050818103600083015261196b8161192f565b9050919050565b6000606082019050611987600083018661101e565b611994602083018561101e565b6119a160408301846113cc565b949350505050565b7f535346207472616e73666572206661696c656400000000000000000000000000600082015250565b60006119df60138361120b565b91506119ea826119a9565b602082019050919050565b60006020820190508181036000830152611a0e816119d2565b9050919050565b6000604082019050611a2a60008301856113cc565b611a3760208301846113cc565b9392505050565b6000608082019050611a5360008301876113cc565b611a6060208301866113cc565b611a6d604083018561101e565b611a7a606083018461101e565b95945050505050565b7f496e76616c6964205469636b65744e4654000000000000000000000000000000600082015250565b6000611ab960118361120b565b9150611ac482611a83565b602082019050919050565b60006020820190508181036000830152611ae881611aac565b9050919050565b7f496e76616c696420536574746c656d656e740000000000000000000000000000600082015250565b6000611b2560128361120b565b9150611b3082611aef565b602082019050919050565b60006020820190508181036000830152611b5481611b18565b9050919050565b7f496e76616c6964204576656e744e465400000000000000000000000000000000600082015250565b6000611b9160108361120b565b9150611b9c82611b5b565b602082019050919050565b60006020820190508181036000830152611bc081611b84565b9050919050565b7f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160008201527f6464726573730000000000000000000000000000000000000000000000000000602082015250565b6000611c2360268361120b565b9150611c2e82611bc7565b604082019050919050565b60006020820190508181036000830152611c5281611c16565b9050919050565b7f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e6572600082015250565b6000611c8f60208361120b565b9150611c9a82611c59565b602082019050919050565b60006020820190508181036000830152611cbe81611c82565b905091905056fea2646970667358221220ff1e32bc9e60d4eb1766c179dfc848558675a8625d9bf380bf7f1c911434f9d564736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_EVENTNFTADDRESS = "eventNFTAddress";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PLATFORMWALLET = "platformWallet";

    public static final String FUNC_PURCHASETICKET = "purchaseTicket";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SETCONTRACTS = "setContracts";

    public static final String FUNC_SETTLEMENTADDRESS = "settlementAddress";

    public static final String FUNC_SSFTOKEN = "ssfToken";

    public static final String FUNC_TICKETNFTADDRESS = "ticketNFTAddress";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    public static final Event TICKETPURCHASED_EVENT = new Event("TicketPurchased",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>() {
        }));;

    @Deprecated
    protected PurchaseRouter(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PurchaseRouter(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PurchaseRouter(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PurchaseRouter(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT,
            transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
        DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static List<TicketPurchasedEventResponse> getTicketPurchasedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TICKETPURCHASED_EVENT,
            transactionReceipt);
        ArrayList<TicketPurchasedEventResponse> responses = new ArrayList<TicketPurchasedEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TicketPurchasedEventResponse typedResponse = new TicketPurchasedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TicketPurchasedEventResponse getTicketPurchasedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TICKETPURCHASED_EVENT, log);
        TicketPurchasedEventResponse typedResponse = new TicketPurchasedEventResponse();
        typedResponse.log = log;
        typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<TicketPurchasedEventResponse> ticketPurchasedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTicketPurchasedEventFromLog(log));
    }

    public Flowable<TicketPurchasedEventResponse> ticketPurchasedEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TICKETPURCHASED_EVENT));
        return ticketPurchasedEventFlowable(filter);
    }

    public RemoteFunctionCall<String> eventNFTAddress() {
        final Function function = new Function(FUNC_EVENTNFTADDRESS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> platformWallet() {
        final Function function = new Function(FUNC_PLATFORMWALLET, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> purchaseTicket(String buyer, BigInteger ticketId,
        BigInteger sessionId) {
        final Function function = new Function(FUNC_PURCHASETICKET,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, buyer),
                new org.web3j.abi.datatypes.generated.Uint256(ticketId),
                new org.web3j.abi.datatypes.generated.Uint256(sessionId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(FUNC_RENOUNCEOWNERSHIP, Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setContracts(String _ticketNFT, String _settlement,
        String _eventNFT) {
        final Function function = new Function(FUNC_SETCONTRACTS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT),
                new org.web3j.abi.datatypes.Address(160, _settlement),
                new org.web3j.abi.datatypes.Address(160, _eventNFT)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> settlementAddress() {
        final Function function = new Function(FUNC_SETTLEMENTADDRESS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ssfToken() {
        final Function function = new Function(FUNC_SSFTOKEN, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ticketNFTAddress() {
        final Function function = new Function(FUNC_TICKETNFTADDRESS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(FUNC_TRANSFEROWNERSHIP,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static PurchaseRouter load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new PurchaseRouter(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PurchaseRouter load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        return new PurchaseRouter(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PurchaseRouter load(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return new PurchaseRouter(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PurchaseRouter load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return new PurchaseRouter(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, credentials, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, transactionManager, contractGasProvider,
            getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, transactionManager, gasPrice, gasLimit,
            getDeploymentBinary(), encodedConstructor);
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class TicketPurchasedEventResponse extends BaseEventResponse {
        public BigInteger ticketId;

        public String buyer;

        public BigInteger sessionId;

        public BigInteger price;
    }
}
