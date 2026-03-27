package com.ssafy.cheket.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
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
import org.web3j.tuples.generated.Tuple4;
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
public class Settlement extends Contract {
    public static final String BINARY = "0x60803461010557601f611b7438819003918201601f19168301916001600160401b0383118484101761010a5780849260209460405283398101031261010557516001600160a01b038116908190036101055760008054336001600160a01b03198216811783556040519290916001600160a01b0316907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09080a381156100c35750600180546001600160a01b031916919091179055604051611a5390816101218239f35b62461bcd60e51b815260206004820152601360248201527f496e76616c6964205353462061646472657373000000000000000000000000006044820152606490fd5b600080fd5b634e487b7160e01b600052604160045260246000fdfe608080604052600436101561001357600080fd5b600090813560e01c90816308df7dc8146116f65750806323af4f4d1461160e5780632f5eb679146116cd57806332e61ba6146102625780633812ae6b1461023857806341ea03ba146116a45780634c58238014611652578063616060621461160e578063627c82f71461151c578063715018a6146114c257806388dae6a8146114a45780638da5cb5b1461147d578063bf989b6e1461125e578063c77836f914611235578063c8336b53146109db578063e037503f146109b2578063e346d62f14610934578063e796a6eb14610291578063e7a956a714610262578063ec35e33714610238578063f2fde38b14610172578063fa2af9da146101495763fb74dabc1461011e57600080fd5b346101465780600319360112610146576001546040516001600160a01b039091168152602090f35b80fd5b50346101465780600319360112610146576005546040516001600160a01b039091168152602090f35b50346101465760203660031901126101465761018c611761565b6101946119c5565b6001600160a01b031680156101e45781546001600160a01b03198116821783556001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08380a380f35b60405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608490fd5b50346101465760203660031901126101465760406020916004358152600783522054604051908152f35b50346101465760203660031901126101465760ff60406020926004358152600884522054166040519015158152f35b5034610146576060366003190112610146576004356102ae61174b565b604435916102ba6119c5565b80845260086020526102d360ff604086205416156117b0565b6001600160a01b0382169182156108ff576002546001600160a01b0316906102fc8215156117f0565b6004546001600160a01b031680156108c6576040516331a9108f60e11b815260048101879052602081602481855afa80156108365786918991610886575b506001600160a01b03160361084157604051630e75722360e41b81526004810187905292602084602481855afa938415610836578894610802575b5060405163285a238960e11b815260048101889052938885602481865afa80156106f85786908a968b91610779575b50036107415760405163402ff0db60e01b815260048101879052608081602481865afa8015610736576020918b91610703575b50604460405180958193630f3d7f6360e11b83528a600484015260248301525afa9182156106f85789926106bf575b508115610683576127109161041a91611970565b04928488526007602052836040892054106106465761048c6020858a95888752600783526040872061044d838254611963565b905560015460405163a9059cbb60e01b81526001600160a01b039283166004820152602481019390935291938492909116908290889082906044820190565b03925af180156105eb57849061060b575b6104a79150611983565b813b156105e757604051630b106a5d60e41b81526004810188905260036024820152838160448183875af19081156105eb5784916105f6575b50506005546001600160a01b0316823b156105ce57604051906323b872dd60e01b82528760048301526024820152876044820152838160648183875af19081156105eb5784916105d2575b50506005546001600160a01b0316823b156105ce5760848492836040519586948593634db9bbfd60e11b855260048501528a60248501528b604485015260648401525af180156105c3576105aa575b505060207f279ac10417b4a21068fc6d3150dd2e5fc45320cb722f60a07d37a113a3bf3acd91604051908152a480f35b816105b49161182f565b6105bf57843861057a565b8480fd5b6040513d84823e3d90fd5b8380fd5b816105dc9161182f565b6105e757823861052b565b8280fd5b6040513d86823e3d90fd5b816106009161182f565b6105e75782386104e0565b506020813d60201161063e575b816106256020938361182f565b810103126105ce576106396104a791611867565b61049d565b3d9150610618565b60405162461bcd60e51b8152602060048201526015602482015274496e73756666696369656e74206465706f7369747360581b6044820152606490fd5b60405162461bcd60e51b8152602060048201526014602482015273526566756e64206e6f7420617661696c61626c6560601b6044820152606490fd5b9091506020813d6020116106f0575b816106db6020938361182f565b810103126106eb57519038610406565b600080fd5b3d91506106ce565b6040513d8b823e3d90fd5b610725915060803d60801161072f575b61071d818361182f565b810190611874565b50509050386103d7565b503d610713565b6040513d8c823e3d90fd5b60405162461bcd60e51b815260206004820152601060248201526f0a6cae6e6d2dedc40dad2e6dac2e8c6d60831b6044820152606490fd5b965050503d808a873e61078c818761182f565b8501610120868203126107fa57855190602087015196604081015167ffffffffffffffff81116107fe57826107c29183016118f3565b5060a081015167ffffffffffffffff81116107fe5760e0926107e59183016118f3565b50015160ff8116036107fa57869095386103a4565b8980fd5b8c80fd5b9093506020813d60201161082e575b8161081e6020938361182f565b810103126106eb57519238610375565b3d9150610811565b6040513d8a823e3d90fd5b60405162461bcd60e51b815260206004820152601960248201527f4275796572206973206e6f74207469636b6574206f776e6572000000000000006044820152606490fd5b9150506020813d6020116108be575b816108a26020938361182f565b810103126108ba576108b486916118df565b3861033a565b8780fd5b3d9150610895565b60405162461bcd60e51b8152602060048201526011602482015270151a58dad95d139195081b9bdd081cd95d607a1b6044820152606490fd5b60405162461bcd60e51b815260206004820152600d60248201526c24b73b30b634b210313abcb2b960991b6044820152606490fd5b50346101465760203660031901126101465761094e611761565b6109566119c5565b6001600160a01b0316801561097b576001600160601b0360a01b600654161760065580f35b60405162461bcd60e51b815260206004820152600f60248201526e496e76616c6964206164647265737360881b6044820152606490fd5b50346101465780600319360112610146576003546040516001600160a01b039091168152602090f35b5034610146576109ea36611777565b91906109f46119c5565b8082526008602052610a0d60ff604084205416156117b0565b6002546001600160a01b031692610a258415156117f0565b6003546001600160a01b031680156111f75760405163402ff0db60e01b815260048101849052608081602481895afa9081156111ec5790839186916111ca575b50036111855782845260076020526040842054938415611152576001546040516370a0823160e01b815230600482015290602090829060249082906001600160a01b03165afa9081156105c357908691839161111d575b50106110d857604051630fa2795760e41b81526004810184905281816024818a5afa9081156105c3578291611047575b5080511561101057805193610b008561189d565b94610b0e604051968761182f565b808652610b1d601f199161189d565b01366020870137815195610b308761189d565b96610b3e604051988961182f565b808852610b4d601f199161189d565b01366020890137839884995b84518b1015610c8f57610b6c8b866118b5565b516040519063c985162960e01b8252600482015286816024818b5afa8015610c845787918891610c2f575b50610ba28d8c6118b5565b52610bad8c8a6118b5565b6001600160a01b039182169052610bc48c8a6118b5565b511615610bea57610be2600191610bdb8d8c6118b5565b519061178d565b9a0199610b59565b60405162461bcd60e51b815260206004820152601a60248201527f496e76616c6964207374616b65686f6c6465722077616c6c65740000000000006044820152606490fd5b9150503d8088833e610c41818361182f565b8101906080818303126108ba57610c57816118df565b91602082015167ffffffffffffffff81116107fa578291610c7b91604094016118f3565b50015138610b97565b6040513d89823e3d90fd5b889293945089612710899203610fcb57848752600860205260408720805460ff19166001179055600954926000198414610fb757600184016009556040516080810181811067ffffffffffffffff821117610fa357604052878152600360208201888152604083018581526060840191428352888d52600a60205260408d209451855551600185015551600284015551910155879588965b84518a818a1015610f1557506000198101908111610f01578803610ee457610d4f8185611963565b868b52600b60205260408b206001600160a01b03610d6d8b896118b5565b516001600160a01b03911681168d52602091825260408d20839055600154610dd6929184918116908f90610da18f8d6118b5565b5160405163a9059cbb60e01b815291166001600160a01b03166004820152602481019390935291938492839182906044820190565b03925af1908c8215610ed8578a928a928d9291610e5b575b50917f59e5c638e8e7ab669e805847b18203cf00e4ab4d0688c3da8e486aba4cc4fed26040610e41610e53979694610e2860019a97611983565b8d610e39828c8060a01b03926118b5565b5116956118b5565b518151908782526020820152a361178d565b970196610d27565b9493505050506020823d8211610ed0575b81610e796020938361182f565b81010312610ecc5760019289897f59e5c638e8e7ab669e805847b18203cf00e4ab4d0688c3da8e486aba4cc4fed26040610e418f610e28610ebc610e539a611867565b95979a5050949697505050610dee565b8b80fd5b3d9150610e6c565b604051903d90823e3d90fd5b612710610efb610ef48a8a6118b5565b5186611970565b04610d4f565b634e487b7160e01b8b52601160045260248bfd5b87848c888489803b15610f9f57819060246040518094819363356b56b760e21b83528960048401525af18015610f945785926020977feb5d1444f0733df81e76f927d8cba3817d08b9b0512d8e77a8cdf3eabc96cd1c938993610f84575b5050604051908152a4604051908152f35b81610f8e9161182f565b89610f73565b6040513d88823e3d90fd5b5080fd5b634e487b7160e01b8a52604160045260248afd5b634e487b7160e01b88526011600452602488fd5b60405162461bcd60e51b815260206004820152601760248201527f546f74616c20627073206d7573742062652031303030300000000000000000006044820152606490fd5b60405162461bcd60e51b815260206004820152600f60248201526e4e6f207374616b65686f6c6465727360881b6044820152606490fd5b90503d8083833e611058818361182f565b8101906020818303126105e75780519067ffffffffffffffff82116105ce57019080601f830112156105e757815161108f8161189d565b9261109d604051948561182f565b81845260208085019260051b8201019283116105bf57602001905b8282106110c85750505038610aec565b81518152602091820191016110b8565b60405162461bcd60e51b815260206004820152601860248201527f496e73756666696369656e74205353462062616c616e636500000000000000006044820152606490fd5b9150506020813d60201161114a575b816111396020938361182f565b81010312610f9f5785905138610abc565b3d915061112c565b60405162461bcd60e51b815260206004820152600b60248201526a4e6f206465706f7369747360a81b6044820152606490fd5b60405162461bcd60e51b815260206004820152601c60248201527f4576656e744964206d69736d6174636820666f722073657373696f6e000000006044820152606490fd5b6111e3915060803d60801161072f5761071d818361182f565b50505038610a65565b6040513d87823e3d90fd5b60405162461bcd60e51b815260206004820152601660248201527514dd185ad95a1bdb19195c939195081b9bdd081cd95d60521b6044820152606490fd5b50346101465780600319360112610146576004546040516001600160a01b039091168152602090f35b503461014657608036600319011261014657611278611761565b61128061174b565b6044356001600160a01b038116908190036105ce576064356001600160a01b03811692908390036105bf576112b36119c5565b6006549360ff8560a01c16611442576001600160a01b031690811561140a576001600160a01b03169081156113cc57821561139357831561134e576001600160601b0360a01b60025416176002556001600160601b0360a01b60035416176003556001600160601b0360a01b60045416176004556001600160601b0360a01b6005541617600555600160a01b9060ff60a01b19161760065580f35b60405162461bcd60e51b815260206004820152601760248201527f496e76616c696420706c6174666f726d2077616c6c65740000000000000000006044820152606490fd5b60405162461bcd60e51b8152602060048201526011602482015270125b9d985b1a5908151a58dad95d139195607a1b6044820152606490fd5b60405162461bcd60e51b8152602060048201526016602482015275125b9d985b1a590814dd185ad95a1bdb19195c93919560521b6044820152606490fd5b60405162461bcd60e51b815260206004820152601060248201526f125b9d985b1a5908115d995b9d13919560821b6044820152606490fd5b60405162461bcd60e51b8152602060048201526013602482015272105b1c9958591e481a5b9a5d1a585b1a5e9959606a1b6044820152606490fd5b5034610146578060031936011261014657546040516001600160a01b039091168152602090f35b50346101465780600319360112610146576020600954604051908152f35b50346101465780600319360112610146576114db6119c5565b80546001600160a01b03198116825581906001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a380f35b50346101465761152b36611777565b6006546001600160a01b031633036115d35780156115995760407fe12932a1eadd33d3bfc1aa59a4fb97aa39e72add425752b05db040d68987aa1191838552600760205281852061157d82825461178d565b905583855260076020528185205482519182526020820152a280f35b60405162461bcd60e51b81526020600482015260126024820152710416d6f756e74206d757374206265203e20360741b6044820152606490fd5b60405162461bcd60e51b815260206004820152601360248201527227b7363c90283ab931b430b9b2a937baba32b960691b6044820152606490fd5b503461014657604036600319011261014657604060209161162d61174b565b6004358252600b84528282206001600160a01b03909116825283522054604051908152f35b5034610146576020366003190112610146576004358152600a602090815260409182902080546001820154600283015460039093015485519283529382015292830152606082015280608081015b0390f35b50346101465780600319360112610146576006546040516001600160a01b039091168152602090f35b50346101465780600319360112610146576002546040516001600160a01b039091168152602090f35b905034610f9f576020366003190112610f9f576116a0604082936004358152600a6020522080549060018101546003600283015492015492859094939260609260808301968352602083015260408201520152565b602435906001600160a01b03821682036106eb57565b600435906001600160a01b03821682036106eb57565b60409060031901126106eb576004359060243590565b9190820180921161179a57565b634e487b7160e01b600052601160045260246000fd5b156117b757565b60405162461bcd60e51b8152602060048201526011602482015270105b1c9958591e48199a5b985b1a5e9959607a1b6044820152606490fd5b156117f757565b60405162461bcd60e51b815260206004820152601060248201526f115d995b9d139195081b9bdd081cd95d60821b6044820152606490fd5b90601f8019910116810190811067ffffffffffffffff82111761185157604052565b634e487b7160e01b600052604160045260246000fd5b519081151582036106eb57565b91908260809103126106eb5781519160208101519161189a606060408401519301611867565b90565b67ffffffffffffffff81116118515760051b60200190565b80518210156118c95760209160051b010190565b634e487b7160e01b600052603260045260246000fd5b51906001600160a01b03821682036106eb57565b81601f820112156106eb5780519067ffffffffffffffff82116118515760405192611928601f8401601f19166020018561182f565b828452602083830101116106eb5760005b82811061194e57505060206000918301015290565b80602080928401015182828701015201611939565b9190820391821161179a57565b8181029291811591840414171561179a57565b1561198a57565b60405162461bcd60e51b815260206004820152601360248201527214d4d1881d1c985b9cd9995c8819985a5b1959606a1b6044820152606490fd5b6000546001600160a01b031633036119d957565b606460405162461bcd60e51b815260206004820152602060248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726044820152fdfea2646970667358221220e2f8f9f11db3ccece2aebddeeb0f6cddcd32ebdca6f23770bc5718c7d5d3441064736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_DISTRIBUTIONS = "distributions";

    public static final String FUNC_EVENTNFTADDRESS = "eventNFTAddress";

    public static final String FUNC_FINALIZESESSION = "finalizeSession";

    public static final String FUNC_GETDISTRIBUTION = "getDistribution";

    public static final String FUNC_GETSESSIONDEPOSITS = "getSessionDeposits";

    public static final String FUNC_GETSETTLEMENT = "getSettlement";

    public static final String FUNC_ISSESSIONFINALIZED = "isSessionFinalized";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PLATFORMWALLET = "platformWallet";

    public static final String FUNC_PURCHASEROUTER = "purchaseRouter";

    public static final String FUNC_RECORDDEPOSIT = "recordDeposit";

    public static final String FUNC_REFUND = "refund";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SESSIONDEPOSITS = "sessionDeposits";

    public static final String FUNC_SESSIONFINALIZED = "sessionFinalized";

    public static final String FUNC_SETCONTRACTS = "setContracts";

    public static final String FUNC_SETPURCHASEROUTER = "setPurchaseRouter";

    public static final String FUNC_SETTLEMENTS = "settlements";

    public static final String FUNC_SSFTOKEN = "ssfToken";

    public static final String FUNC_STAKEHOLDERNFTADDRESS = "stakeholderNFTAddress";

    public static final String FUNC_TICKETNFTADDRESS = "ticketNFTAddress";

    public static final String FUNC_TOTALSETTLEMENTS = "totalSettlements";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event DEPOSITRECORDED_EVENT = new Event("DepositRecorded",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>() {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    public static final Event PAID_EVENT = new Event("Paid",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>() {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event REFUNDED_EVENT = new Event("Refunded",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event SESSIONFINALIZED_EVENT = new Event("SessionFinalized",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>() {
        }));;

    @Deprecated
    protected Settlement(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Settlement(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Settlement(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Settlement(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<DepositRecordedEventResponse> getDepositRecordedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEPOSITRECORDED_EVENT,
            transactionReceipt);
        ArrayList<DepositRecordedEventResponse> responses = new ArrayList<DepositRecordedEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DepositRecordedEventResponse typedResponse = new DepositRecordedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newTotal = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DepositRecordedEventResponse getDepositRecordedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEPOSITRECORDED_EVENT, log);
        DepositRecordedEventResponse typedResponse = new DepositRecordedEventResponse();
        typedResponse.log = log;
        typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.newTotal = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DepositRecordedEventResponse> depositRecordedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDepositRecordedEventFromLog(log));
    }

    public Flowable<DepositRecordedEventResponse> depositRecordedEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEPOSITRECORDED_EVENT));
        return depositRecordedEventFlowable(filter);
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

    public static List<PaidEventResponse> getPaidEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PAID_EVENT,
            transactionReceipt);
        ArrayList<PaidEventResponse> responses = new ArrayList<PaidEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PaidEventResponse typedResponse = new PaidEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.settlementId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.stakeholder = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.shareBps = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PaidEventResponse getPaidEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PAID_EVENT, log);
        PaidEventResponse typedResponse = new PaidEventResponse();
        typedResponse.log = log;
        typedResponse.settlementId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.stakeholder = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.shareBps = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<PaidEventResponse> paidEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPaidEventFromLog(log));
    }

    public Flowable<PaidEventResponse> paidEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PAID_EVENT));
        return paidEventFlowable(filter);
    }

    public static List<RefundedEventResponse> getRefundedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(REFUNDED_EVENT,
            transactionReceipt);
        ArrayList<RefundedEventResponse> responses = new ArrayList<RefundedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RefundedEventResponse typedResponse = new RefundedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static RefundedEventResponse getRefundedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(REFUNDED_EVENT, log);
        RefundedEventResponse typedResponse = new RefundedEventResponse();
        typedResponse.log = log;
        typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<RefundedEventResponse> refundedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getRefundedEventFromLog(log));
    }

    public Flowable<RefundedEventResponse> refundedEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REFUNDED_EVENT));
        return refundedEventFlowable(filter);
    }

    public static List<SessionFinalizedEventResponse> getSessionFinalizedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SESSIONFINALIZED_EVENT,
            transactionReceipt);
        ArrayList<SessionFinalizedEventResponse> responses = new ArrayList<SessionFinalizedEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SessionFinalizedEventResponse typedResponse = new SessionFinalizedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.settlementId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.eventId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.totalAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SessionFinalizedEventResponse getSessionFinalizedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SESSIONFINALIZED_EVENT, log);
        SessionFinalizedEventResponse typedResponse = new SessionFinalizedEventResponse();
        typedResponse.log = log;
        typedResponse.settlementId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.eventId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.totalAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<SessionFinalizedEventResponse> sessionFinalizedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSessionFinalizedEventFromLog(log));
    }

    public Flowable<SessionFinalizedEventResponse> sessionFinalizedEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SESSIONFINALIZED_EVENT));
        return sessionFinalizedEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> distributions(BigInteger param0, String param1) {
        final Function function = new Function(FUNC_DISTRIBUTIONS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0),
                new org.web3j.abi.datatypes.Address(160, param1)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> eventNFTAddress() {
        final Function function = new Function(FUNC_EVENTNFTADDRESS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> finalizeSession(BigInteger sessionId, BigInteger eventId) {
        final Function function = new Function(FUNC_FINALIZESESSION,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(sessionId),
                new org.web3j.abi.datatypes.generated.Uint256(eventId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> getDistribution(BigInteger settlementId, String wallet) {
        final Function function = new Function(FUNC_GETDISTRIBUTION,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(settlementId),
                new org.web3j.abi.datatypes.Address(160, wallet)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> getSessionDeposits(BigInteger sessionId) {
        final Function function = new Function(FUNC_GETSESSIONDEPOSITS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(sessionId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple4<BigInteger, BigInteger, BigInteger, BigInteger>> getSettlement(
        BigInteger settlementId) {
        final Function function = new Function(FUNC_GETSETTLEMENT,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(settlementId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }));
        return new RemoteFunctionCall<Tuple4<BigInteger, BigInteger, BigInteger, BigInteger>>(function,
            new Callable<Tuple4<BigInteger, BigInteger, BigInteger, BigInteger>>() {
                @Override
                public Tuple4<BigInteger, BigInteger, BigInteger, BigInteger> call() throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple4<BigInteger, BigInteger, BigInteger, BigInteger>(
                        (BigInteger) results.get(0).getValue(), (BigInteger) results.get(1).getValue(),
                        (BigInteger) results.get(2).getValue(), (BigInteger) results.get(3).getValue());
                }
            });
    }

    public RemoteFunctionCall<Boolean> isSessionFinalized(BigInteger sessionId) {
        final Function function = new Function(FUNC_ISSESSIONFINALIZED,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(sessionId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
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

    public RemoteFunctionCall<String> purchaseRouter() {
        final Function function = new Function(FUNC_PURCHASEROUTER, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> recordDeposit(BigInteger sessionId, BigInteger amount) {
        final Function function = new Function(FUNC_RECORDDEPOSIT,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(sessionId),
                new org.web3j.abi.datatypes.generated.Uint256(amount)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> refund(BigInteger sessionId, String buyer, BigInteger tokenId) {
        final Function function = new Function(FUNC_REFUND,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(sessionId),
                new org.web3j.abi.datatypes.Address(160, buyer),
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(FUNC_RENOUNCEOWNERSHIP, Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> sessionDeposits(BigInteger param0) {
        final Function function = new Function(FUNC_SESSIONDEPOSITS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Boolean> sessionFinalized(BigInteger param0) {
        final Function function = new Function(FUNC_SESSIONFINALIZED,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> setContracts(String _eventNFT, String _stakeholderNFT,
        String _ticketNFT, String _platformWallet) {
        final Function function = new Function(FUNC_SETCONTRACTS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _eventNFT),
                new org.web3j.abi.datatypes.Address(160, _stakeholderNFT),
                new org.web3j.abi.datatypes.Address(160, _ticketNFT),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setPurchaseRouter(String _purchaseRouter) {
        final Function function = new Function(FUNC_SETPURCHASEROUTER,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _purchaseRouter)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple4<BigInteger, BigInteger, BigInteger, BigInteger>> settlements(BigInteger param0) {
        final Function function = new Function(FUNC_SETTLEMENTS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }));
        return new RemoteFunctionCall<Tuple4<BigInteger, BigInteger, BigInteger, BigInteger>>(function,
            new Callable<Tuple4<BigInteger, BigInteger, BigInteger, BigInteger>>() {
                @Override
                public Tuple4<BigInteger, BigInteger, BigInteger, BigInteger> call() throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple4<BigInteger, BigInteger, BigInteger, BigInteger>(
                        (BigInteger) results.get(0).getValue(), (BigInteger) results.get(1).getValue(),
                        (BigInteger) results.get(2).getValue(), (BigInteger) results.get(3).getValue());
                }
            });
    }

    public RemoteFunctionCall<String> ssfToken() {
        final Function function = new Function(FUNC_SSFTOKEN, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> stakeholderNFTAddress() {
        final Function function = new Function(FUNC_STAKEHOLDERNFTADDRESS, Arrays.<Type>asList(),
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

    public RemoteFunctionCall<BigInteger> totalSettlements() {
        final Function function = new Function(FUNC_TOTALSETTLEMENTS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(FUNC_TRANSFEROWNERSHIP,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Settlement load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new Settlement(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Settlement load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        return new Settlement(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Settlement load(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return new Settlement(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Settlement load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return new Settlement(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Settlement> deploy(Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider, String _ssfToken) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken)));
        return deployRemoteCall(Settlement.class, web3j, credentials, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    public static RemoteCall<Settlement> deploy(Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider, String _ssfToken) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken)));
        return deployRemoteCall(Settlement.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Settlement> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit, String _ssfToken) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken)));
        return deployRemoteCall(Settlement.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Settlement> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
        BigInteger gasLimit, String _ssfToken) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken)));
        return deployRemoteCall(Settlement.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
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

    public static class DepositRecordedEventResponse extends BaseEventResponse {
        public BigInteger sessionId;

        public BigInteger amount;

        public BigInteger newTotal;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class PaidEventResponse extends BaseEventResponse {
        public BigInteger settlementId;

        public String stakeholder;

        public BigInteger amount;

        public BigInteger shareBps;
    }

    public static class RefundedEventResponse extends BaseEventResponse {
        public BigInteger sessionId;

        public String buyer;

        public BigInteger tokenId;

        public BigInteger amount;
    }

    public static class SessionFinalizedEventResponse extends BaseEventResponse {
        public BigInteger settlementId;

        public BigInteger eventId;

        public BigInteger sessionId;

        public BigInteger totalAmount;
    }
}
