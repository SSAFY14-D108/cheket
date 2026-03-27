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
    public static final String BINARY = "0x60803461010557601f611b3a38819003918201601f19168301916001600160401b0383118484101761010a5780849260209460405283398101031261010557516001600160a01b038116908190036101055760008054336001600160a01b03198216811783556040519290916001600160a01b0316907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09080a381156100c35750600180546001600160a01b031916919091179055604051611a1990816101218239f35b62461bcd60e51b815260206004820152601360248201527f496e76616c6964205353462061646472657373000000000000000000000000006044820152606490fd5b600080fd5b634e487b7160e01b600052604160045260246000fdfe608080604052600436101561001357600080fd5b600090813560e01c90816308df7dc8146116b75750806323af4f4d146115cf5780632f5eb6791461168e57806332e61ba6146102625780633812ae6b1461023857806341ea03ba146116655780634c5823801461161357806361606062146115cf578063627c82f7146114dd578063715018a61461148357806388dae6a8146114655780638da5cb5b1461143e578063bf989b6e1461121f578063c77836f9146111f6578063c8336b53146109de578063e037503f146109b5578063e346d62f14610937578063e796a6eb14610291578063e7a956a714610262578063ec35e33714610238578063f2fde38b14610172578063fa2af9da146101495763fb74dabc1461011e57600080fd5b346101465780600319360112610146576001546040516001600160a01b039091168152602090f35b80fd5b50346101465780600319360112610146576005546040516001600160a01b039091168152602090f35b50346101465760203660031901126101465761018c611727565b61019461198b565b6001600160a01b031680156101e45781546001600160a01b03198116821783556001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08380a380f35b60405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608490fd5b50346101465760203660031901126101465760406020916004358152600783522054604051908152f35b50346101465760203660031901126101465760ff60406020926004358152600884522054166040519015158152f35b5034610146576060366003190112610146576004356102ae61170c565b604435916102ba61198b565b80845260086020526102d360ff60408620541615611776565b6001600160a01b038216918215610902576002546001600160a01b03166102fb8115156117b6565b6004546001600160a01b03169081156108c9576040516331a9108f60e11b815260048101879052602081602481865afa801561060b578691899161088d575b506001600160a01b03160361084857604051630e75722360e41b81526004810187905290602082602481865afa91821561060b578892610814575b5060405163285a238960e11b815260048101889052918883602481875afa801561070a5786908a948b9161078b575b50036107535760405163402ff0db60e01b815260048101879052608081602481865afa8015610748576020918b91610715575b50604460405180958193630f3d7f6360e11b835288600484015260248301525afa91821561070a5789926106d2575b508115610696576127109161041a91611936565b04928488526007602052836040892054106106595760208461048b92878b526007835260408b2061044c838254611929565b905560015460405163a9059cbb60e01b81526001600160a01b0392831660048201526024810193909352919384929091169082908c9082906044820190565b03925af1801561060b57889061061a575b6104a69150611949565b813b1561061657604051630b106a5d60e41b81526004810187905260036024820152878160448183875af1801561060b576105f5575b50600554879291906001600160a01b0316823b156105cd57604051906323b872dd60e01b82528760048301526024820152876044820152838160648183875af19081156105ea5784916105d1575b50506005546001600160a01b0316823b156105cd5760848492836040519586948593634db9bbfd60e11b855260048501528a60248501528b604485015260648401525af180156105c2576105a9575b505060207f279ac10417b4a21068fc6d3150dd2e5fc45320cb722f60a07d37a113a3bf3acd91604051908152a480f35b816105b3916117f5565b6105be578438610579565b8480fd5b6040513d84823e3d90fd5b8380fd5b816105db916117f5565b6105e657823861052a565b8280fd5b6040513d86823e3d90fd5b8761060391989293986117f5565b9590386104dc565b6040513d8a823e3d90fd5b8680fd5b506020813d602011610651575b81610634602093836117f5565b8101031261064d576106486104a69161182d565b61049c565b8780fd5b3d9150610627565b60405162461bcd60e51b8152602060048201526015602482015274496e73756666696369656e74206465706f7369747360581b6044820152606490fd5b60405162461bcd60e51b8152602060048201526014602482015273526566756e64206e6f7420617661696c61626c6560601b6044820152606490fd5b9091506020813d602011610702575b816106ee602093836117f5565b810103126106fe57519038610406565b8880fd5b3d91506106e1565b6040513d8b823e3d90fd5b610737915060803d608011610741575b61072f81836117f5565b81019061183a565b50509050386103d7565b503d610725565b6040513d8c823e3d90fd5b60405162461bcd60e51b815260206004820152601060248201526f0a6cae6e6d2dedc40dad2e6dac2e8c6d60831b6044820152606490fd5b945050503d808a853e61079e81856117f5565b83016101208482031261080c57835190602085015194604081015167ffffffffffffffff811161081057826107d49183016118b9565b5060a081015167ffffffffffffffff81116108105760e0926107f79183016118b9565b50015160ff81160361080c57869093386103a4565b8980fd5b8c80fd5b9091506020813d602011610840575b81610830602093836117f5565b8101031261064d57519038610375565b3d9150610823565b60405162461bcd60e51b815260206004820152601960248201527f4275796572206973206e6f74207469636b6574206f776e6572000000000000006044820152606490fd5b9150506020813d6020116108c1575b816108a9602093836117f5565b8101031261064d576108bb86916118a5565b3861033a565b3d915061089c565b60405162461bcd60e51b8152602060048201526011602482015270151a58dad95d139195081b9bdd081cd95d607a1b6044820152606490fd5b60405162461bcd60e51b815260206004820152600d60248201526c24b73b30b634b210313abcb2b960991b6044820152606490fd5b503461014657602036600319011261014657610951611727565b61095961198b565b6001600160a01b0316801561097e576001600160601b0360a01b600654161760065580f35b60405162461bcd60e51b815260206004820152600f60248201526e496e76616c6964206164647265737360881b6044820152606490fd5b50346101465780600319360112610146576003546040516001600160a01b039091168152602090f35b5034610146576109ed3661173d565b91906109f761198b565b8082526008602052610a1060ff60408420541615611776565b6002546001600160a01b0316610a278115156117b6565b6003546001600160a01b031680156111b85760405163402ff0db60e01b815260048101849052608081602481865afa9081156111ad57908691869161118b575b50036111465782845260076020526040842054938415611113576001546040516370a0823160e01b815230600482015290602090829060249082906001600160a01b03165afa9081156105c25790869183916110da575b5010611095578060249360405194858092630fa2795760e41b82528a60048301525afa928315611088578193610ff1575b50825115610fba57825192610b0384611863565b93610b1160405195866117f5565b808552610b20601f1991611863565b01366020860137805194610b3386611863565b95610b4160405197886117f5565b808752610b50601f1991611863565b01366020880137829783985b83518a1015610c9257610b6f8a8561187b565b516040519063c985162960e01b8252600482015285816024818a5afa8015610c875786918791610c32575b50610ba58c8b61187b565b52610bb08b8961187b565b6001600160a01b039182169052610bc78b8961187b565b511615610bed57610be5600191610bde8c8b61187b565b5190611753565b990198610b5c565b60405162461bcd60e51b815260206004820152601a60248201527f496e76616c6964207374616b65686f6c6465722077616c6c65740000000000006044820152606490fd5b9150503d8087833e610c4481836117f5565b81019060808183031261061657610c5a816118a5565b91602082015167ffffffffffffffff81116106fe578291610c7e91604094016118b9565b50015138610b9a565b6040513d88823e3d90fd5b84935088876127108a9303610f7557848652600860205260408620805460ff19166001179055600954916000198314610f6157600183016009556040516080810181811067ffffffffffffffff821117610f4d57604052858152600360208201888152604083018481526060840191428352878c52600a60205260408c209451855551600185015551600284015551910155869487955b835180881015610f15576000198101908111610f01578703610ee457610d4f8184611929565b858a52600b60205260408a206001600160a01b03610d6d8a8861187b565b516001600160a01b03911681168c52602091825260408c20839055600154610dd6929184918116908e90610da18e8c61187b565b5160405163a9059cbb60e01b815291166001600160a01b03166004820152602481019390935291938492839182906044820190565b03925af1908b8215610ed857899289928c9291610e5b575b50917f59e5c638e8e7ab669e805847b18203cf00e4ab4d0688c3da8e486aba4cc4fed26040610e41610e53979694610e2860019a97611949565b8c610e39828c8060a01b039261187b565b51169561187b565b518151908782526020820152a3611753565b960195610d29565b9493505050506020823d8211610ed0575b81610e79602093836117f5565b81010312610ecc5760019288887f59e5c638e8e7ab669e805847b18203cf00e4ab4d0688c3da8e486aba4cc4fed26040610e418e610e28610ebc610e539a61182d565b95979a5050949697505050610dee565b8a80fd5b3d9150610e6c565b604051903d90823e3d90fd5b612710610efb610ef4898961187b565b5185611936565b04610d4f565b634e487b7160e01b8a52601160045260248afd5b6020868a85827feb5d1444f0733df81e76f927d8cba3817d08b9b0512d8e77a8cdf3eabc96cd1c858a604051908152a4604051908152f35b634e487b7160e01b89526041600452602489fd5b634e487b7160e01b87526011600452602487fd5b60405162461bcd60e51b815260206004820152601760248201527f546f74616c20627073206d7573742062652031303030300000000000000000006044820152606490fd5b60405162461bcd60e51b815260206004820152600f60248201526e4e6f207374616b65686f6c6465727360881b6044820152606490fd5b9092503d8084833e61100381836117f5565b8101906020818303126105cd5780519067ffffffffffffffff82116105be57019080601f830112156105cd57815161103a81611863565b9261104860405194856117f5565b81845260208085019260051b82010192831161108457602001905b828210611074575050509138610aef565b8151815260209182019101611063565b8580fd5b50604051903d90823e3d90fd5b60405162461bcd60e51b815260206004820152601860248201527f496e73756666696369656e74205353462062616c616e636500000000000000006044820152606490fd5b9150506020813d60201161110b575b816110f6602093836117f5565b810103126111075785905138610abe565b5080fd5b3d91506110e9565b60405162461bcd60e51b815260206004820152600b60248201526a4e6f206465706f7369747360a81b6044820152606490fd5b60405162461bcd60e51b815260206004820152601c60248201527f4576656e744964206d69736d6174636820666f722073657373696f6e000000006044820152606490fd5b6111a4915060803d6080116107415761072f81836117f5565b50505038610a67565b6040513d87823e3d90fd5b60405162461bcd60e51b815260206004820152601660248201527514dd185ad95a1bdb19195c939195081b9bdd081cd95d60521b6044820152606490fd5b50346101465780600319360112610146576004546040516001600160a01b039091168152602090f35b503461014657608036600319011261014657611239611727565b61124161170c565b6044356001600160a01b038116908190036105cd576064356001600160a01b03811692908390036105be5761127461198b565b6006549360ff8560a01c16611403576001600160a01b03169081156113cb576001600160a01b031690811561138d57821561135457831561130f576001600160601b0360a01b60025416176002556001600160601b0360a01b60035416176003556001600160601b0360a01b60045416176004556001600160601b0360a01b6005541617600555600160a01b9060ff60a01b19161760065580f35b60405162461bcd60e51b815260206004820152601760248201527f496e76616c696420706c6174666f726d2077616c6c65740000000000000000006044820152606490fd5b60405162461bcd60e51b8152602060048201526011602482015270125b9d985b1a5908151a58dad95d139195607a1b6044820152606490fd5b60405162461bcd60e51b8152602060048201526016602482015275125b9d985b1a590814dd185ad95a1bdb19195c93919560521b6044820152606490fd5b60405162461bcd60e51b815260206004820152601060248201526f125b9d985b1a5908115d995b9d13919560821b6044820152606490fd5b60405162461bcd60e51b8152602060048201526013602482015272105b1c9958591e481a5b9a5d1a585b1a5e9959606a1b6044820152606490fd5b5034610146578060031936011261014657546040516001600160a01b039091168152602090f35b50346101465780600319360112610146576020600954604051908152f35b503461014657806003193601126101465761149c61198b565b80546001600160a01b03198116825581906001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a380f35b5034610146576114ec3661173d565b6006546001600160a01b0316330361159457801561155a5760407fe12932a1eadd33d3bfc1aa59a4fb97aa39e72add425752b05db040d68987aa1191838552600760205281852061153e828254611753565b905583855260076020528185205482519182526020820152a280f35b60405162461bcd60e51b81526020600482015260126024820152710416d6f756e74206d757374206265203e20360741b6044820152606490fd5b60405162461bcd60e51b815260206004820152601360248201527227b7363c90283ab931b430b9b2a937baba32b960691b6044820152606490fd5b50346101465760403660031901126101465760406020916115ee61170c565b6004358252600b84528282206001600160a01b03909116825283522054604051908152f35b5034610146576020366003190112610146576004358152600a602090815260409182902080546001820154600283015460039093015485519283529382015292830152606082015280608081015b0390f35b50346101465780600319360112610146576006546040516001600160a01b039091168152602090f35b50346101465780600319360112610146576002546040516001600160a01b039091168152602090f35b90503461110757602036600319011261110757611661604082936004358152600a6020522080549060018101546003600283015492015492859094939260609260808301968352602083015260408201520152565b602435906001600160a01b038216820361172257565b600080fd5b600435906001600160a01b038216820361172257565b6040906003190112611722576004359060243590565b9190820180921161176057565b634e487b7160e01b600052601160045260246000fd5b1561177d57565b60405162461bcd60e51b8152602060048201526011602482015270105b1c9958591e48199a5b985b1a5e9959607a1b6044820152606490fd5b156117bd57565b60405162461bcd60e51b815260206004820152601060248201526f115d995b9d139195081b9bdd081cd95d60821b6044820152606490fd5b90601f8019910116810190811067ffffffffffffffff82111761181757604052565b634e487b7160e01b600052604160045260246000fd5b5190811515820361172257565b91908260809103126117225781519160208101519161186060606040840151930161182d565b90565b67ffffffffffffffff81116118175760051b60200190565b805182101561188f5760209160051b010190565b634e487b7160e01b600052603260045260246000fd5b51906001600160a01b038216820361172257565b81601f820112156117225780519067ffffffffffffffff821161181757604051926118ee601f8401601f1916602001856117f5565b828452602083830101116117225760005b82811061191457505060206000918301015290565b806020809284010151828287010152016118ff565b9190820391821161176057565b8181029291811591840414171561176057565b1561195057565b60405162461bcd60e51b815260206004820152601360248201527214d4d1881d1c985b9cd9995c8819985a5b1959606a1b6044820152606490fd5b6000546001600160a01b0316330361199f57565b606460405162461bcd60e51b815260206004820152602060248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726044820152fdfea264697066735822122082673e37f7ab024345997b9675761f3347bf001a14093a40eb895a7062374aa464736f6c634300081c0033";

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
